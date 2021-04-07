-- *********************************************************************
-- Update Database Script - FROM domibusConnector 3.5 to 4.0
-- *********************************************************************
-- updates the connector database FROM an 3.5 connector version to 4.0
-- remove all Foreign Keys first before executing this script, otherwise there
-- will be some errors at the end of the script, when the foreign keys are
-- recreated with specific names
--


-- Drop all FKs
-- they will be recreated at the end of the script
-- note: if the FK cannot be found, just remove all FKs from your DB
-- consult the oracle manual to do so:
-- Get all Constraints within schema
--
-- SELECT A.TABLE_NAME,
--       A.COLUMN_NAME,
--       A.CONSTRAINT_NAME,
--      C.OWNER
-- FROM   ALL_CONS_COLUMNS A,
--       ALL_CONSTRAINTS C
-- WHERE  A.CONSTRAINT_NAME = C.CONSTRAINT_NAME
-- AND    C.CONSTRAINT_TYPE = 'R'
-- AND    C.OWNER = '<dbLoginName/schema>';

-- RENAME tables that need to be recreated
RENAME DOMIBUS_CONNECTOR_SEQ_STORE TO BKP_DC_SEQ_STORE;
RENAME DOMIBUS_CONNECTOR_MSG_ERROR TO BKP_DC_MSG_ERROR;
RENAME DOMIBUS_CONNECTOR_MESSAGE_INFO TO BKP_DC_MSG_INFO;
RENAME DOMIBUS_CONNECTOR_EVIDENCES TO BKP_DC_EVIDENCES;

-- CREATE those tables
CREATE TABLE DOMIBUS_CONNECTOR_EVIDENCE
(
    ID            NUMBER(10) not null primary key,
    MESSAGE_ID    NUMBER(10) not null,
    TYPE          VARCHAR2(255),
    EVIDENCE      CLOB,
    DELIVERED_NAT TIMESTAMP,
    DELIVERED_GW  TIMESTAMP,
    UPDATED       TIMESTAMP
);
/

CREATE TABLE DOMIBUS_CONNECTOR_SEQ_STORE
(
    SEQ_NAME  VARCHAR2(255 CHAR) NOT NULL,
    SEQ_VALUE DECIMAL(10, 0)     NOT NULL
);
/

CREATE TABLE DOMIBUS_CONNECTOR_MSG_ERROR
(
    ID            NUMBER(10)    not null primary key,
    MESSAGE_ID    NUMBER(10)    not null,
    ERROR_MESSAGE VARCHAR2(512) not null,
    CREATED       TIMESTAMP     not null,
    ERROR_SOURCE  CLOB,
    DETAILED_TEXT CLOB
);
/

CREATE TABLE DOMIBUS_CONNECTOR_MSG_CONT
(
    ID           NUMBER(10) NOT NULL,
    MESSAGE_ID   NUMBER(10) NOT NULL,
    CONTENT_TYPE VARCHAR2(255),
    CONTENT      BLOB,
    CHECKSUM     CLOB,
    CREATED      TIMESTAMP
);
/

CREATE TABLE DOMIBUS_CONNECTOR_MESSAGE_INFO
(
    ID              NUMBER(10) not null primary key,
    MESSAGE_ID      NUMBER(10) not null unique,
    FROM_PARTY_ID   VARCHAR2(255),
    FROM_PARTY_ROLE VARCHAR2(255),
    TO_PARTY_ID     VARCHAR2(255),
    TO_PARTY_ROLE   VARCHAR2(255),
    ORIGINAL_SENDER VARCHAR2(255),
    FINAL_RECIPIENT VARCHAR2(255),
    SERVICE         VARCHAR2(255),
    ACTION          VARCHAR2(255),
    CREATED         TIMESTAMP  not null,
    UPDATED         TIMESTAMP  not null
);
/

CREATE TABLE DOMIBUS_CONNECTOR_BACKEND_INFO
(
    ID                   NUMBER(10)    NOT NULL,
    BACKEND_NAME         VARCHAR2(255) NOT NULL,
    BACKEND_KEY_ALIAS    VARCHAR2(255) NOT NULL,
    BACKEND_KEY_PASS     VARCHAR2(255),
    BACKEND_SERVICE_TYPE VARCHAR2(255),
    BACKEND_ENABLED      NUMBER(1),
    BACKEND_DEFAULT      NUMBER(1),
    BACKEND_DESCRIPTION  CLOB,
    BACKEND_PUSH_ADDRESS VARCHAR2(512)
);
/

CREATE TABLE DOMIBUS_CONNECTOR_BACK_2_S
(
    DOMIBUS_CONNECTOR_SERVICE_ID VARCHAR2(255) NOT NULL,
    DOMIBUS_CONNECTOR_BACKEND_ID NUMBER(10)    NOT NULL
);
/

CREATE TABLE DOMIBUS_CONNECTOR_BIGDATA
(
    ID          VARCHAR2(255) NOT NULL PRIMARY KEY,
    CHECKSUM    CLOB,
    CREATED     TIMESTAMP,
    MESSAGE_ID  DECIMAL(10, 0),
    LAST_ACCESS TIMESTAMP,
    NAME        CLOB,
    CONTENT     BLOB,
    MIMETYPE    VARCHAR2(255)
);
/

-- MODIFY tables / INSERT data from temporary tables INTO new the new ones
--
-- DOMIBUS_CONNECTOR_SEQ_STORE

INSERT INTO DOMIBUS_CONNECTOR_SEQ_STORE
VALUES ('DOMIBUS_CONNECTOR_MESSAGE.ID',
        (SELECT SEQ_VALUE FROM BKP_DC_SEQ_STORE WHERE SEQ_NAME = 'DOMIBUS_CONNECTOR_MESSAGES.ID'));

INSERT INTO DOMIBUS_CONNECTOR_SEQ_STORE
VALUES ('DOMIBUS_CONNECTOR_EVIDENCE.ID',
        (SELECT SEQ_VALUE FROM BKP_DC_SEQ_STORE WHERE SEQ_NAME = 'DOMIBUS_CONNECTOR_EVIDENCES.ID'));

INSERT INTO DOMIBUS_CONNECTOR_SEQ_STORE
SELECT *
FROM BKP_DC_SEQ_STORE
WHERE SEQ_NAME in ('DOMIBUS_CONNECTOR_MESSAGE_INFO.ID', 'DOMIBUS_CONNECTOR_MSG_ERROR.ID');

-- Drop QUARTZ Tables
DECLARE
    DCON_QRTZ_BLOB_TRIGGERS       VARCHAR2(250):= 'DCON_QRTZ_BLOB_TRIGGERS';
    DCON_QRTZ_CALENDARS           VARCHAR2(250):= 'DCON_QRTZ_CALENDARS';
    DCON_QRTZ_CRON_TRIGGERS       VARCHAR2(250):= 'DCON_QRTZ_CRON_TRIGGERS';
    DCON_QRTZ_FIRED_TRIGGERS      VARCHAR2(250):= 'DCON_QRTZ_FIRED_TRIGGERS';
    DCON_QRTZ_LOCKS               VARCHAR2(250):= 'DCON_QRTZ_LOCKS';
    DCON_QRTZ_PAUSED_TRIGGER_GRPS VARCHAR2(250):= 'DCON_QRTZ_PAUSED_TRIGGER_GRPS';
    DCON_QRTZ_SCHEDULER_STATE     VARCHAR2(250):= 'DCON_QRTZ_SCHEDULER_STATE';
    DCON_QRTZ_SIMPLE_TRIGGERS     VARCHAR2(250):= 'DCON_QRTZ_SIMPLE_TRIGGERS';
    DCON_QRTZ_SIMPROP_TRIGGERS    VARCHAR2(250):= 'DCON_QRTZ_SIMPROP_TRIGGERS';
    DCON_QRTZ_TRIGGERS            VARCHAR2(250):= 'DCON_QRTZ_TRIGGERS';
    DCON_QRTZ_JOB_DETAILS         VARCHAR2(250):= 'DCON_QRTZ_JOB_DETAILS';
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_BLOB_TRIGGERS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_CALENDARS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_CRON_TRIGGERS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_FIRED_TRIGGERS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_LOCKS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_PAUSED_TRIGGER_GRPS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_SCHEDULER_STATE || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_SIMPLE_TRIGGERS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_SIMPROP_TRIGGERS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_TRIGGERS || ' CASCADE CONSTRAINTS';
    EXECUTE IMMEDIATE 'DROP TABLE ' || DCON_QRTZ_JOB_DETAILS || ' CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        -- "table not found" exceptions are ignored, anything else is raised
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

-- DOMIBUS_CONNECTOR_SERVICE
ALTER TABLE DOMIBUS_CONNECTOR_SERVICE
    MODIFY SERVICE VARCHAR2(255);

ALTER TABLE DOMIBUS_CONNECTOR_SERVICE
    MODIFY SERVICE_TYPE VARCHAR2(512);

insert into DOMIBUS_CONNECTOR_SERVICE values ('n.a.', 'n.a.');

-- DOMIBUS_CONNECTOR_PARTY
ALTER TABLE DOMIBUS_CONNECTOR_PARTY
    MODIFY PARTY_ID VARCHAR2(255);

insert into DOMIBUS_CONNECTOR_PARTY
values ('n.a.', 'n.a.', 'n.a.');


ALTER TABLE DOMIBUS_CONNECTOR_PARTY
    MODIFY ROLE VARCHAR2(255);

ALTER TABLE DOMIBUS_CONNECTOR_PARTY
    MODIFY PARTY_ID_TYPE VARCHAR2(512);

-- DOMIBUS_CONNECTOR_MSG_ERROR
INSERT INTO DOMIBUS_CONNECTOR_MSG_ERROR
SELECT ID, MESSAGE_ID, ERROR_MESSAGE, CREATED, ERROR_SOURCE, DETAILED_TEXT FROM BKP_DC_MSG_ERROR;

-- DOMIBUS_CONNECTOR_MESSAGE_INFO
-- INSERT INTO DOMIBUS_CONNECTOR_MESSAGE_INFO
-- SELECT * FROM BKP_DC_MSG_INFO;
insert into DOMIBUS_CONNECTOR_MESSAGE_INFO
select
    B.ID,
    B.MESSAGE_ID,
    CASE
        when FROM_PARTY_ID is not null and FROM_PARTY_ROLE is not null
            then  (select PARTY_ID from DOMIBUS_CONNECTOR_PARTY FP where FP.PARTY_ID=FROM_PARTY_ID and FP.ROLE=FROM_PARTY_ROLE)
        else 'n.a.'
        end
        as FROM_PARTY_ID,
    CASE
        when FROM_PARTY_ID is not null and FROM_PARTY_ROLE is not null
            then  (select ROLE from DOMIBUS_CONNECTOR_PARTY FP where FP.PARTY_ID=FROM_PARTY_ID and FP.ROLE=FROM_PARTY_ROLE)
        else 'n.a.'
        end
        as FROM_PARTY_ROLE,
    CASE
        when TO_PARTY_ID is not null and TO_PARTY_ROLE is not null
            then  (select PARTY_ID from DOMIBUS_CONNECTOR_PARTY FP where FP.PARTY_ID=TO_PARTY_ID and FP.ROLE=TO_PARTY_ROLE)
        else 'n.a.'
        end
        as TO_PARTY_ID,
    CASE
        when TO_PARTY_ID is not null and TO_PARTY_ROLE is not null
            then  (select ROLE from DOMIBUS_CONNECTOR_PARTY FP where FP.PARTY_ID=TO_PARTY_ID and FP.ROLE=TO_PARTY_ROLE)
        else 'n.a.'
        end
        as TO_PARTY_ROLE,
    ORIGINAL_SENDER,
    FINAL_RECIPIENT,
    CASE
        when B.SERVICE is not null
            then  (select SERVICE from DOMIBUS_CONNECTOR_SERVICE S where S.SERVICE=B.SERVICE)
        else 'n.a.'
        end
        as FK_SERVICE,
    CASE
        when B.ACTION is not null
            then  (select ACTION from DOMIBUS_CONNECTOR_ACTION A where A.ACTION=B.ACTION)
        else 'n.a.'
        end
        as FK_ACTION,
    CREATED,
    UPDATED
from BKP_DC_MSG_INFO B;

-- DOMIBUS_CONNECTOR_MESSAGE
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGES RENAME TO DOMIBUS_CONNECTOR_MESSAGE;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    MODIFY ID NUMBER(10);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE RENAME COLUMN NAT_MESSAGE_ID TO BACKEND_MESSAGE_ID;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    MODIFY CONFIRMED TIMESTAMP;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    MODIFY REJECTED TIMESTAMP;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    MODIFY DELIVERED_GW TIMESTAMP;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    MODIFY UPDATED TIMESTAMP;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE RENAME COLUMN DELIVERED_NAT TO DELIVERED_BACKEND;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    ADD BACKEND_NAME VARCHAR2(255);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    ADD CONNECTOR_MESSAGE_ID VARCHAR2(255 CHAR);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    ADD CREATED TIMESTAMP;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    ADD HASH_VALUE_TEMP CLOB;
UPDATE DOMIBUS_CONNECTOR_MESSAGE
SET HASH_VALUE_TEMP=HASH_VALUE;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE
    DROP COLUMN HASH_VALUE;
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE RENAME COLUMN HASH_VALUE_TEMP TO HASH_VALUE;

UPDATE domibus_connector_message SET connector_message_id='_migrate_' || SYS_GUID() where CONNECTOR_MESSAGE_ID is null;


-- DOMIBUS_CONNECTOR_EVIDENCES
INSERT INTO DOMIBUS_CONNECTOR_EVIDENCE
SELECT * FROM BKP_DC_EVIDENCES;

-- DOMIBUS_CONNECTOR_ACTION
ALTER TABLE DOMIBUS_CONNECTOR_ACTION
    MODIFY ACTION VARCHAR2(255);
insert into DOMIBUS_CONNECTOR_ACTION values ('n.a.', 0);

-- Add the constraints
ALTER TABLE DOMIBUS_CONNECTOR_SEQ_STORE ADD CONSTRAINT PK_DC_SEQ_STORE PRIMARY KEY (SEQ_NAME);
ALTER TABLE DOMIBUS_CONNECTOR_MSG_ERROR ADD CONSTRAINT FK_DC_MSG_ERROR FOREIGN KEY (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGE (ID);
ALTER TABLE DOMIBUS_CONNECTOR_MSG_CONT ADD CONSTRAINT PK_DC_MSG_01 PRIMARY KEY (ID);
ALTER TABLE DOMIBUS_CONNECTOR_MSG_CONT ADD CONSTRAINT FK_DC_CON_04 FOREIGN KEY (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGE (ID);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE_INFO ADD CONSTRAINT FK_DC_MSG_INFO_1 foreign key (ACTION) REFERENCES DOMIBUS_CONNECTOR_ACTION (ACTION);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE_INFO ADD CONSTRAINT FK_DC_MSG_INFO_2 foreign key (SERVICE) REFERENCES DOMIBUS_CONNECTOR_SERVICE (SERVICE);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE_INFO ADD CONSTRAINT FK_DC_MSG_INFO_3 foreign key (FROM_PARTY_ID, FROM_PARTY_ROLE) references DOMIBUS_CONNECTOR_PARTY(PARTY_ID, ROLE);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE_INFO ADD CONSTRAINT FK_DC_MSG_INFO_4 foreign key (TO_PARTY_ID, TO_PARTY_ROLE) references DOMIBUS_CONNECTOR_PARTY(PARTY_ID, ROLE);
ALTER TABLE DOMIBUS_CONNECTOR_MESSAGE_INFO ADD CONSTRAINT FK_DC_MSG_INFO_I foreign key (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGE (ID);
ALTER TABLE DOMIBUS_CONNECTOR_BACKEND_INFO ADD CONSTRAINT PK_DC_BACK_01 PRIMARY KEY (ID);
ALTER TABLE DOMIBUS_CONNECTOR_BACKEND_INFO ADD CONSTRAINT UN_DC_BACK_01 UNIQUE (BACKEND_NAME);
ALTER TABLE DOMIBUS_CONNECTOR_BACKEND_INFO ADD CONSTRAINT UN_DC_BACK_02 UNIQUE (BACKEND_KEY_ALIAS);
ALTER TABLE DOMIBUS_CONNECTOR_BACK_2_S ADD CONSTRAINT FK_DC_BACK2S_01 FOREIGN KEY (DOMIBUS_CONNECTOR_BACKEND_ID) REFERENCES DOMIBUS_CONNECTOR_BACKEND_INFO (ID);
ALTER TABLE DOMIBUS_CONNECTOR_BACK_2_S ADD CONSTRAINT FK_DC_BACK2S_02 FOREIGN KEY (DOMIBUS_CONNECTOR_SERVICE_ID) REFERENCES DOMIBUS_CONNECTOR_SERVICE (SERVICE);
ALTER TABLE DOMIBUS_CONNECTOR_BIGDATA ADD CONSTRAINT FK_DC_BIGDATA FOREIGN KEY (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGE (ID);
ALTER TABLE DOMIBUS_CONNECTOR_EVIDENCE ADD CONSTRAINT FK_DC_EVIDENCES FOREIGN KEY (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGE (ID);

-- Delete temporary tables
DROP TABLE BKP_DC_SEQ_STORE;
DROP TABLE BKP_DC_MSG_ERROR;
DROP TABLE BKP_DC_MSG_INFO;
DROP TABLE BKP_DC_EVIDENCES;