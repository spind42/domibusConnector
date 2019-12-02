-- *********************************************************************
-- Update Database Script - from domibusConnector 4.0 to 4.1
-- *********************************************************************
-- updates the connector database from an 3.5 connector version to 4.0
-- remove all Foreign Keys first before executing this script, otherwise there
-- will be some errors at the end of the script, when the foreign keys are
-- recreated with specific names
-- 


CREATE TABLE  "DOMIBUS_CONNECTOR_PROPERTY"
(
	"PROPERTY_NAME" VARCHAR2(512) NOT NULL,
	"PROPERTY_VALUE" VARCHAR2(1024) NULL
)
;

/* Create Primary Keys, Indexes, Uniques, Checks, Triggers */

ALTER TABLE  "DOMIBUS_CONNECTOR_PROPERTY" 
 ADD CONSTRAINT "PK_DOMIBUS_CONN_03"
	PRIMARY KEY ("PROPERTY_NAME") 
 USING INDEX
;

/* Create Tables */

CREATE TABLE  "DOMIBUS_CONNECTOR_USER"
(
	"ID" NUMBER(10) NOT NULL,
	"USERNAME" VARCHAR2(50) NOT NULL,
	"ROLE" VARCHAR2(50) NOT NULL,
	"LOCKED" NUMBER(1) DEFAULT 0 NOT NULL,
	"NUMBER_OF_GRACE_LOGINS" NUMBER(2) DEFAULT 5 NOT NULL,
	"GRACE_LOGINS_USED" NUMBER(2) DEFAULT 0 NOT NULL,
	"CREATED" TIMESTAMP NOT NULL
)
;

CREATE TABLE  "DOMIBUS_CONNECTOR_USER_PWD"
(
	"ID" NUMBER(10) NOT NULL,
	"USER_ID" NUMBER(10) NOT NULL,
	"PASSWORD" VARCHAR2(1024) NOT NULL,
	"SALT" VARCHAR2(512) NOT NULL,
	"CURRENT_PWD" NUMBER(1) DEFAULT 0 NOT NULL,
	"INITIAL_PWD" NUMBER(1) DEFAULT 0 NOT NULL,
	"CREATED" TIMESTAMP NOT NULL
)
;

ALTER TABLE "DOMIBUS_CONNECTOR_EVIDENCE"
 ADD CONNECTOR_MESSAGE_ID VARCHAR2(255)
 ;

CREATE INDEX "IXFK_DOMIBUS_CONN_EV01"
 ON  "DOMIBUS_CONNECTOR_EVIDENCE" ("CONNECTOR_MESSAGE_ID")
;

/* Create Primary Keys, Indexes, Uniques, Checks, Triggers */

ALTER TABLE  "DOMIBUS_CONNECTOR_USER" 
 ADD CONSTRAINT "PK_DOMIBUS_CONNECTOR_USER"
	PRIMARY KEY ("ID") 
 USING INDEX
;

ALTER TABLE  "DOMIBUS_CONNECTOR_USER_PWD" 
 ADD CONSTRAINT "PK_DOMIBUS_CONNECTOR_USER_01"
	PRIMARY KEY ("ID") 
 USING INDEX
;

CREATE INDEX "IXFK_DOMIBUS_CONN_DOMIBUS01"   
 ON  "DOMIBUS_CONNECTOR_USER_PWD" ("USER_ID") 
;

/* Create Foreign Key Constraints */

ALTER TABLE  "DOMIBUS_CONNECTOR_USER_PWD" 
 ADD CONSTRAINT "FK_DOMIBUS_CONN_DOMIBUS_CON_06"
	FOREIGN KEY ("USER_ID") REFERENCES  "DOMIBUS_CONNECTOR_USER" ("ID")
;

INSERT INTO DOMIBUS_CONNECTOR_USER (ID, USERNAME, ROLE, LOCKED, NUMBER_OF_GRACE_LOGINS, GRACE_LOGINS_USED, CREATED) VALUES (1, 'admin', 'ADMIN', 0, 5, 0, current_timestamp);
INSERT INTO DOMIBUS_CONNECTOR_USER_PWD (ID, USER_ID, PASSWORD, SALT, CURRENT_PWD, INITIAL_PWD, CREATED) VALUES (1, 1, '2bf5e637d0d82a75ca43e3be85df2c23febffc0cc221f5e010937005df478a19b5eaab59fe7e4e97f6b43ba648c169effd432e19817f386987d058c239236306', '5b424031616564356639', 1, 1, current_timestamp);

INSERT INTO DOMIBUS_CONNECTOR_USER (ID, USERNAME, ROLE, LOCKED, NUMBER_OF_GRACE_LOGINS, GRACE_LOGINS_USED, CREATED) VALUES (2, 'user', 'USER', 0, 5, 0, current_timestamp);
INSERT INTO DOMIBUS_CONNECTOR_USER_PWD (ID, USER_ID, PASSWORD, SALT, CURRENT_PWD, INITIAL_PWD, CREATED) VALUES (2, 2, '2bf5e637d0d82a75ca43e3be85df2c23febffc0cc221f5e010937005df478a19b5eaab59fe7e4e97f6b43ba648c169effd432e19817f386987d058c239236306', '5b424031616564356639', 1, 1, current_timestamp);

INSERT INTO DOMIBUS_CONNECTOR_SEQ_STORE VALUES ('DOMIBUS_CONNECTOR_USER.ID', 3);
INSERT INTO DOMIBUS_CONNECTOR_SEQ_STORE VALUES ('DOMIBUS_CONNECTOR_USER_PWD.ID', 3);

DROP TABLE DOMIBUS_WEBADMIN_PROPERTY;
DROP TABLE DOMIBUS_WEBADMIN_USER;

ALTER TABLE DOMIBUS_CONNECTOR_MSG_ERROR MODIFY ERROR_MESSAGE VARCHAR(2048);
