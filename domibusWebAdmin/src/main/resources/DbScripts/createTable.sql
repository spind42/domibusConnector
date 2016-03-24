CREATE TABLE DOMIBUS_WEBADMIN_USER (
  USERNAME  VARCHAR2(30)  NOT NULL,
  PASSWORD  VARCHAR2(150)  NOT NULL,
  SALT VARCHAR2(64) NOT NULL,
  ROLE	    VARCHAR2(20) NOT NULL,
  PRIMARY KEY (USERNAME)
);

INSERT INTO DOMIBUS_WEBADMIN_USER (USERNAME, PASSWORD, SALT, ROLE) VALUES ('admin', '2bf5e637d0d82a75ca43e3be85df2c23febffc0cc221f5e010937005df478a19b5eaab59fe7e4e97f6b43ba648c169effd432e19817f386987d058c239236306', '5b424031616564356639', 'admin');

CREATE TABLE DOMIBUS_WEBADMIN_PROPERTIES (
  PROPERTIES_KEY VARCHAR2(30), 
  PROPERTIES_VALUE VARCHAR2(100 BYTE)
);