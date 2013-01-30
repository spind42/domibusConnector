DROP TABLE ECODEX_EVIDENCES;
DROP TABLE ECODEX_MESSAGES;
DROP TABLE ECODEX_SEQ_STORE;

CREATE TABLE ECODEX_MESSAGES (
	ID NUMBER(10) NOT NULL,
	EBMS_MESSAGE_ID VARCHAR2(255) UNIQUE,
	NAT_MESSAGE_ID VARCHAR2(255) UNIQUE,
	CONVERSATION_ID VARCHAR2(255),
	DIRECTION VARCHAR2(10),
	HASH_VALUE VARCHAR2(1000),
	CONFIRMED TIMESTAMP,
	REJECTED TIMESTAMP,
	DELIVERED_NAT TIMESTAMP,
	DELIVERED_GW TIMESTAMP,
	UPDATED TIMESTAMP,
	PRIMARY KEY (ID)
);

CREATE TABLE ECODEX_EVIDENCES (
	ID  NUMBER(10) NOT NULL,
	MESSAGE_ID  NUMBER(10) NOT NULL,
	TYPE VARCHAR2(255),
	EVIDENCE CLOB,
	DELIVERED_NAT TIMESTAMP,
	DELIVERED_GW TIMESTAMP,
	UPDATED TIMESTAMP,
	PRIMARY KEY (ID),
	FOREIGN KEY (MESSAGE_ID) REFERENCES ECODEX_MESSAGES (ID)
);

CREATE TABLE ECODEX_SEQ_STORE (
	SEQ_NAME VARCHAR2(255) NOT NULL,
	SEQ_VALUE  NUMBER(10) NOT NULL,
	PRIMARY KEY(SEQ_NAME)
);

INSERT INTO ECODEX_SEQ_STORE VALUES ('ECODEX_MESSAGES.ID', 0);
INSERT INTO ECODEX_SEQ_STORE VALUES ('ECODEX_EVIDENCES.ID', 0);

commit;