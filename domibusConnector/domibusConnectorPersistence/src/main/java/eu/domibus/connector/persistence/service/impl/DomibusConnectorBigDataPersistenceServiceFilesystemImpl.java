package eu.domibus.connector.persistence.service.impl;

import eu.domibus.connector.domain.model.DomibusConnectorBigDataReference;
import eu.domibus.connector.domain.model.DomibusConnectorMessage;
import eu.domibus.connector.persistence.service.DomibusConnectorBigDataPersistenceService;
import eu.domibus.connector.persistence.service.exceptions.LargeFileDeletionException;
import eu.domibus.connector.persistence.service.exceptions.PersistenceException;
import eu.domibus.connector.persistence.spring.DomibusConnectorFilesystemPersistenceProperties;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.domibus.connector.persistence.spring.PersistenceProfiles.STORAGE_FS_PROFILE_NAME;

//initialized by DomibusConnectorPersistenceContext.class
@Profile(STORAGE_FS_PROFILE_NAME)
public class DomibusConnectorBigDataPersistenceServiceFilesystemImpl implements DomibusConnectorBigDataPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomibusConnectorBigDataPersistenceServiceFilesystemImpl.class);

    @Autowired
    DomibusConnectorFilesystemPersistenceProperties filesystemPersistenceProperties;

    //setter
    public void setFilesystemPersistenceProperties(DomibusConnectorFilesystemPersistenceProperties filesystemPersistenceProperties) {
        this.filesystemPersistenceProperties = filesystemPersistenceProperties;

    }

    @Override
    public DomibusConnectorBigDataReference getReadableDataSource(DomibusConnectorBigDataReference ref) {
        if (!(ref instanceof FileBasedDomibusConnectorBigDataReference)) {
            throw new PersistenceException(String.format("Can only getReadableDataSource for a [%s] BigDataReference but the provided reference was of type [%s]",
                    FileBasedDomibusConnectorBigDataReference.class, ref.getClass()));
        }
        FileBasedDomibusConnectorBigDataReference fileBasedReference = (FileBasedDomibusConnectorBigDataReference)ref;

        String storageIdReference = fileBasedReference.getStorageIdReference();
        Path filePath = getStoragePath().resolve(storageIdReference);

        try {
            FileInputStream fis = new FileInputStream(filePath.toFile());
            if (fileBasedReference.getEncryptionKey() != null) {
                fileBasedReference.setInputStream(generateDecryptedInputStream(fileBasedReference, fis));
            } else {
                fileBasedReference.setInputStream(fis);
            }

        } catch (FileNotFoundException e) {
            throw new PersistenceException(String.format("Could not found the required file [{}]!", filePath), e);
        }

        return fileBasedReference;
    }


    @Override
    public DomibusConnectorBigDataReference createDomibusConnectorBigDataReference(String connectorMessageId, String documentName, String documentContentType) {
        return createDomibusConnectorBigDataReference(null, connectorMessageId, documentName, documentContentType);
    }

    @Override
    public DomibusConnectorBigDataReference createDomibusConnectorBigDataReference(InputStream input, String connectorMessageId, String documentName, String documentContentType) {
        FileBasedDomibusConnectorBigDataReference bigDataReference = new FileBasedDomibusConnectorBigDataReference();
        bigDataReference.setName(documentName);
        bigDataReference.setMimetype(documentContentType);

        String messageFolderName = connectorMessageId;
        Path messageFolder = getStoragePath().resolve(messageFolderName); // //new File(f.getAbsolutePath() + File.separator + folder);
        try {
            LOGGER.debug("Creating message folder [{}]", messageFolder);
            Files.createDirectory(messageFolder);
        } catch (java.nio.file.FileAlreadyExistsException alreadyExists) {
            if (!Files.isDirectory(messageFolder)) {
                throw new RuntimeException(String.format("Cannot use directory path [%s] because it is a file!"));
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Cannot create directory [%s]", messageFolder), e);
        }

        String storageFileName = UUID.randomUUID().toString();
        String storageFileRelativPathName = createReferenceName(messageFolderName, storageFileName);
        bigDataReference.setStorageIdReference(storageFileRelativPathName);

        Path storageFile = messageFolder.resolve(storageFileName);
        LOGGER.debug("Storage file path is [{}]", storageFile.toAbsolutePath());

        try {
            Files.createFile(storageFile);
        } catch (FileAlreadyExistsException alreadyExistsException) {
            throw new PersistenceException(String.format("Error while creating file [%s], looks like the file has already written! You can only write once to a bigDataReference OutputStream!", storageFile), alreadyExistsException);
        } catch (IOException e) {
            throw new PersistenceException(String.format("Error while creating file [%s]", storageFile), e);
        }


        if (input != null) {
            try (OutputStream os = getOutputStream(bigDataReference)) {
                IOUtils.copy(input, os);
                bigDataReference.setOutputStream(os);
            } catch(FileNotFoundException e){
                throw new PersistenceException(String.format("Error while creating FileOutpuStream for file [%s]", storageFile), e);
            } catch(IOException e){
                throw new PersistenceException(String.format("Error while writing to file [%s]", storageFile), e);
            }
        } else {
            try {
                bigDataReference.setOutputStream(getOutputStream(bigDataReference));
            } catch (FileNotFoundException e) {
                throw new PersistenceException(String.format("Error while creating FileOutpuStream for file [%s]", storageFile), e);
            }
        }


        return bigDataReference;
    }

    OutputStream getOutputStream(FileBasedDomibusConnectorBigDataReference dataReference) throws FileNotFoundException {
        Path storageFile = getStoragePath().resolve(dataReference.getStorageIdReference());
        LOGGER.debug("Storage file path is [{}]", storageFile.toAbsolutePath());



        if (!Files.exists(storageFile)) {
            throw new PersistenceException(String.format("The requested file [%s] does not exist yet! Looks like this method is not called correctly!", storageFile));
        }

        FileOutputStream fos = new FileOutputStream(storageFile.toFile());
        OutputStream outputStream;
        if (filesystemPersistenceProperties.isEncryptionActive()) {
            LOGGER.debug("Encryption is activated creating encrypted output stream");
            outputStream = generateEncryptedOutputStream(dataReference, fos);
            return outputStream;
        }
        return fos;

    }

    @Override
    public void deleteDomibusConnectorBigDataReference(DomibusConnectorBigDataReference reference) {

        Path storageFile = getStoragePath().resolve(reference.getStorageIdReference());
        try {
            //TODO: find out which part is blocking the deletion, holding a file handle! OR implement async deletion...?
            Files.delete(storageFile);
        } catch (IOException e) {
            //TODO: make exception configureable so on systems with deactivated timer job, this would be fatal
//            throw new PersistenceException(String.format("Unable to delete file [%s]", storageFile), e);
//            LOGGER.info("Unable to delete file [{}].", storageFile);
//            LOGGER.debug(String.format("Unable to delete file [%s] due exception:", storageFile), e);
            LargeFileDeletionException largeFileDeletionException = new LargeFileDeletionException(String.format("Unable to delete file [%s] due exception:", storageFile), e);
            largeFileDeletionException.setReferenceFailedToDelete(reference);
            throw largeFileDeletionException;
        }
    }

    @Override
    public Map<DomibusConnectorMessage.DomibusConnectorMessageId, List<DomibusConnectorBigDataReference>> getAllAvailableReferences() {
        Path storagePath = getStoragePath();
        try {
            return Files.list(storagePath)
                    .collect(Collectors.toMap(
                            path -> new DomibusConnectorMessage.DomibusConnectorMessageId(path.getFileName().toString()),
                            this::listReferences
                    ));
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while ls files in directory [%s]", storagePath));
        }
    }

    private List<DomibusConnectorBigDataReference> listReferences(Path messageFolder) {
        String messageFolderName = messageFolder.getFileName().toString();
        try {
            return Files.list(messageFolder)
                    .map(p -> p.getFileName().toString())
                    .map(s -> mapMessageFolderAndFileNameToReference(messageFolderName, s))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while listing all files in messageFolder [%s]", messageFolder));
        }
    }

    private DomibusConnectorBigDataReference mapMessageFolderAndFileNameToReference(String messageFolderName, String fileName) {
        String storageIdRef = createReferenceName(messageFolderName, fileName);
        FileBasedDomibusConnectorBigDataReference ref = new FileBasedDomibusConnectorBigDataReference();
        ref.setStorageIdReference(storageIdRef);
        return ref;
    }

    private String createReferenceName(String messageFolderName, String fileName) {
        return messageFolderName + File.separator + fileName;
    }


    private Path getStoragePath() {
        return filesystemPersistenceProperties.getStoragePath();
    }

    @PostConstruct
    public void init() {
        //TODO: check: path writable?
        Path storagePath = filesystemPersistenceProperties.getStoragePath();
        File f = storagePath.toFile();
        if (!f.exists() && filesystemPersistenceProperties.isCreateDir()) {
            LOGGER.info("Creating missing directory path [{}]", storagePath);
            f.mkdirs();
        } else if (!f.exists()) {
            throw new IllegalArgumentException(String.format("The by configuration (%s) provided file path [%s] does not exist an file path creation (%s) is false!",
                    "connector.persistence.filesystem.storage-path", //TODO: property service
                    storagePath,
                    "connector.persistence.filesystem.create-dir") ); //TODO: property service
        }

    }

    InputStream generateDecryptedInputStream(FileBasedDomibusConnectorBigDataReference bigDataReference, InputStream encryptedInputStream) {
        IvParameterSpec ivspec = new IvParameterSpec(Base64Utils.decodeFromString(bigDataReference.getInitVector()));
        SecretKey secretKey = loadFromKeyString(bigDataReference.getEncryptionKey());

        Cipher ci = null;
        try {
            ci = Cipher.getInstance(bigDataReference.getCipherSuite());
            ci.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            CipherInputStream inputStream = new CipherInputStream(encryptedInputStream, ci);
            return inputStream;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

    }

    OutputStream generateEncryptedOutputStream(FileBasedDomibusConnectorBigDataReference bigDataReference, OutputStream outputStream) {

        SecureRandom random;
        try {
            random = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] iv = new byte[128/8];
        random.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);

        String initVector = Base64Utils.encodeToString(ivspec.getIV());
        bigDataReference.setInitVector(initVector);


        SecretKey sKey;
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES"); //TODO: load from properties
            kg.init(random);
            sKey = kg.generateKey();
            bigDataReference.setEncryptionKey(convertSecretKeyToString(sKey)); //TODO: also put configureable part of key there - to avoid having the whole key stored into the database!
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot initialize Key Generator!");
        }

        Cipher ci = null;
        try {
            String cipherSuite = "AES/CBC/PKCS5Padding"; //TODO: load from properties
            ci = Cipher.getInstance(cipherSuite);
            bigDataReference.setCipherSuite(cipherSuite);
            ci.init(Cipher.ENCRYPT_MODE, sKey, ivspec);
            CipherOutputStream cos = new CipherOutputStream(outputStream, ci);
            return cos;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    String convertSecretKeyToString(SecretKey key) {
        String alg = key.getAlgorithm();
        String base64KeyString = Base64Utils.encodeToString(key.getEncoded());
        return alg + "#@#" + base64KeyString;
    }

    SecretKey loadFromKeyString(String str) {
        String[] split = str.split("#@#");
        if (split.length != 2) {
            throw new IllegalArgumentException(String.format("The provided string [%s] does not match the format! Maybe the data is corrupted!", str));
        }
        String keyAlgorithm = split[0];
        byte[] keyBinary = Base64Utils.decodeFromString(split[1]);
        SecretKeySpec skey = new SecretKeySpec(keyBinary, keyAlgorithm);
        return skey;
    }


    /**
     *
     *
     *
     * BigDataPersistenceServiceFilesystemImpl implementation of BigDataReference
     *  this class is internal api do not use this class outside the BigDataPersistenceServiceFilesystemImpl
     */
    static final class FileBasedDomibusConnectorBigDataReference extends DomibusConnectorBigDataReference {

        /**
         *
         */
        private static final long serialVersionUID = 1;

        transient DomibusConnectorBigDataPersistenceServiceFilesystemImpl fsService;

        transient InputStream inputStream;

        transient OutputStream outputStream;

        boolean readable;

        boolean writeable;

        private String encryptionKey;

        private String initVector;

        private String cipherSuite;

        @Override
        public InputStream getInputStream() throws IOException {
            return this.inputStream;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return this.outputStream;
        }

        @Override
        public boolean isReadable() {
            return readable;
        }

        @Override
        public boolean isWriteable() {
            return writeable;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setOutputStream(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        public void setReadable(boolean readable) {
            this.readable = readable;
        }

        public void setWriteable(boolean writeable) {
            this.writeable = writeable;
        }

        public String getEncryptionKey() {
            return encryptionKey;
        }

        public void setEncryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

        public void setInitVector(String initVector) {
            this.initVector = initVector;
        }

        public String getInitVector() {
            return initVector;
        }

        public String getCipherSuite() {
            return cipherSuite;
        }

        public void setCipherSuite(String cipherSuite) {
            this.cipherSuite = cipherSuite;
        }

    }
}
