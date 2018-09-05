

----------------------- domibusConnector Tables ------------------------------


CREATE TABLE DOMIBUS_CONNECTOR_MESSAGES (
	ID BIGINT NOT NULL,
	EBMS_MESSAGE_ID VARCHAR(255) UNIQUE,
	NAT_MESSAGE_ID VARCHAR(255) UNIQUE,
	CONVERSATION_ID VARCHAR(255),
	DIRECTION VARCHAR(10),
	HASH_VALUE VARCHAR(1000),
	CONFIRMED DATETIME,
	REJECTED DATETIME,
	DELIVERED_NAT DATETIME,
	DELIVERED_GW DATETIME,
	UPDATED TIMESTAMP,
	PRIMARY KEY (ID)
);

CREATE TABLE DOMIBUS_CONNECTOR_EVIDENCES (
	ID BIGINT NOT NULL,
	MESSAGE_ID BIGINT NOT NULL,
	TYPE VARCHAR(255),
	EVIDENCE TEXT,
	DELIVERED_NAT DATETIME,
	DELIVERED_GW DATETIME,
	UPDATED TIMESTAMP,
	PRIMARY KEY (ID),
	FOREIGN KEY (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGES (ID)
);

CREATE TABLE DOMIBUS_CONNECTOR_SEQ_STORE (
	SEQ_NAME VARCHAR(255) NOT NULL,
	SEQ_VALUE BIGINT NOT NULL,
	PRIMARY KEY(SEQ_NAME)
);

CREATE TABLE DOMIBUS_CONNECTOR_PARTY (
	PARTY_ID VARCHAR(50) NOT NULL,
	ROLE VARCHAR(255) NOT NULL,
	PARTY_ID_TYPE VARCHAR(255),
	PRIMARY KEY (PARTY_ID, ROLE)
);

CREATE TABLE DOMIBUS_CONNECTOR_ACTION (
	ACTION VARCHAR(50) NOT NULL,
	PDF_REQUIRED SMALLINT NOT NULL DEFAULT 1,
	PRIMARY KEY (ACTION)
);

CREATE TABLE DOMIBUS_CONNECTOR_SERVICE (
	SERVICE VARCHAR(50) NOT NULL,
	SERVICE_TYPE VARCHAR(255) NOT NULL,
	PRIMARY KEY (SERVICE)
);

CREATE TABLE DOMIBUS_CONNECTOR_MESSAGE_INFO (
	ID  BIGINT NOT NULL,
	MESSAGE_ID  BIGINT UNIQUE NOT NULL,
	FROM_PARTY_ID VARCHAR(50),
	FROM_PARTY_ROLE VARCHAR(50),
	TO_PARTY_ID VARCHAR(50),
	TO_PARTY_ROLE VARCHAR(50),
	ORIGINAL_SENDER VARCHAR(50),
	FINAL_RECIPIENT VARCHAR(50),
	SERVICE VARCHAR(50),
	ACTION VARCHAR(50),
	CREATED DATETIME NOT NULL,
	UPDATED DATETIME NOT NULL,
	PRIMARY KEY (ID),
	FOREIGN KEY (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGES (ID),
	FOREIGN KEY (FROM_PARTY_ID, FROM_PARTY_ROLE) REFERENCES DOMIBUS_CONNECTOR_PARTY (PARTY_ID, ROLE),
	FOREIGN KEY (TO_PARTY_ID, TO_PARTY_ROLE) REFERENCES DOMIBUS_CONNECTOR_PARTY (PARTY_ID, ROLE),
	FOREIGN KEY (SERVICE) REFERENCES DOMIBUS_CONNECTOR_SERVICE (SERVICE),
	FOREIGN KEY (ACTION) REFERENCES DOMIBUS_CONNECTOR_ACTION (ACTION)
);

CREATE TABLE DOMIBUS_CONNECTOR_MSG_ERROR (
	ID  BIGINT NOT NULL,
	MESSAGE_ID  BIGINT NOT NULL,
	ERROR_MESSAGE VARCHAR(255) NOT NULL,
	DETAILED_TEXT VARCHAR(2048),
	ERROR_SOURCE VARCHAR(255),
	CREATED DATETIME NOT NULL,
	PRIMARY KEY (ID),
	FOREIGN KEY (MESSAGE_ID) REFERENCES DOMIBUS_CONNECTOR_MESSAGES (ID)
);

----------------------- Quartz Scheduler Tables ------------------------------

CREATE TABLE DCON_QRTZ_JOB_DETAILS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME   VARCHAR(250) NOT NULL,
    IS_DURABLE VARCHAR(1) NOT NULL,
    IS_NONCONCURRENT VARCHAR(1) NOT NULL,
    IS_UPDATE_DATA VARCHAR(1) NOT NULL,
    REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE DCON_QRTZ_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT(13) NULL,
    PREV_FIRE_TIME BIGINT(13) NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT(13) NOT NULL,
    END_TIME BIGINT(13) NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT(2) NULL,
    JOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
        REFERENCES DCON_QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE DCON_QRTZ_SIMPLE_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT(7) NOT NULL,
    REPEAT_INTERVAL BIGINT(12) NOT NULL,
    TIMES_TRIGGERED BIGINT(10) NOT NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES DCON_QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE DCON_QRTZ_CRON_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(200) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES DCON_QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE DCON_QRTZ_SIMPROP_TRIGGERS
  (          
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR(1) NULL,
    BOOL_PROP_2 VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
    REFERENCES DCON_QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE DCON_QRTZ_BLOB_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BLOB NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES DCON_QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE DCON_QRTZ_CALENDARS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME  VARCHAR(200) NOT NULL,
    CALENDAR BLOB NOT NULL,
    PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);

CREATE TABLE DCON_QRTZ_PAUSED_TRIGGER_GRPS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP  VARCHAR(200) NOT NULL, 
    PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);

CREATE TABLE DCON_QRTZ_FIRED_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT(13) NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_NONCONCURRENT VARCHAR(1) NULL,
    REQUESTS_RECOVERY VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);

CREATE TABLE DCON_QRTZ_SCHEDULER_STATE
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT(13) NOT NULL,
    CHECKIN_INTERVAL BIGINT(13) NOT NULL,
    PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);

CREATE TABLE DCON_QRTZ_LOCKS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR(40) NOT NULL, 
    PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);


commit;