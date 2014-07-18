CREATE TABLE TWO_FACTOR_USER
(
  UUID            VARCHAR2(40 CHAR)             NOT NULL,
  LAST_UPDATED    INTEGER                       NOT NULL,
  VERSION_NUMBER  INTEGER                       NOT NULL,
  DELETED_ON      INTEGER,
  LOGINID         VARCHAR2(100 CHAR)            NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_USER IS 'One row for each user in the two factor system';

COMMENT ON COLUMN TWO_FACTOR_USER.UUID IS 'uuid identifies each row in this table';

COMMENT ON COLUMN TWO_FACTOR_USER.LAST_UPDATED IS 'millis since 1970 that this record has been updated';

COMMENT ON COLUMN TWO_FACTOR_USER.VERSION_NUMBER IS 'increments each time this record is stored, for optimistic locking purposes';

COMMENT ON COLUMN TWO_FACTOR_USER.DELETED_ON IS 'If this row is deleted, set a delete date, and delete it later';

COMMENT ON COLUMN TWO_FACTOR_USER.LOGINID IS 'loginid of user, can be null, but is unique';



CREATE UNIQUE INDEX TWO_FACTOR_USER_PK ON TWO_FACTOR_USER
(UUID);


CREATE UNIQUE INDEX TWO_FACTOR_USER_U01 ON TWO_FACTOR_USER
(LOGINID);


ALTER TABLE TWO_FACTOR_USER ADD (
  CONSTRAINT TWO_FACTOR_USER_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_USER_PK);

ALTER TABLE TWO_FACTOR_USER ADD (
  CONSTRAINT TWO_FACTOR_USER_U01
  UNIQUE (LOGINID)
  USING INDEX TWO_FACTOR_USER_U01);

  
  
  
  
  
  
CREATE TABLE two_factor_browser
(
  UUID                  VARCHAR2(40 CHAR)       NOT NULL,
  USER_UUID             VARCHAR2(40 CHAR),
  LAST_UPDATED          INTEGER                 NOT NULL,
  VERSION_NUMBER        INTEGER                 NOT NULL,
  DELETED_ON            INTEGER,
  BROWSER_TRUSTED_UUID  VARCHAR2(40 CHAR)       NOT NULL,
  TRUSTED_BROWSER       VARCHAR2(1 BYTE)        NOT NULL,
  WHEN_TRUSTED          INTEGER
);

COMMENT ON TABLE two_factor_browser IS 'row for each browser that a user has';

COMMENT ON COLUMN two_factor_browser.UUID IS 'uuid for this record';

COMMENT ON COLUMN two_factor_browser.USER_UUID IS 'foreign key to the user table';

COMMENT ON COLUMN two_factor_browser.LAST_UPDATED IS 'millis since 1970 when this browser was last updated';

COMMENT ON COLUMN two_factor_browser.VERSION_NUMBER IS 'increments each time the record is stored, for optimistic locking';

COMMENT ON COLUMN two_factor_browser.DELETED_ON IS 'if this row is to be deleted, this is the number of millis since 1970, then a week later it will actually be deleted';

COMMENT ON COLUMN two_factor_browser.BROWSER_TRUSTED_UUID IS 'the cookie value of the browser, encrypted';

COMMENT ON COLUMN two_factor_browser.TRUSTED_BROWSER IS 'if the browser is trusted (indicated by user)';

COMMENT ON COLUMN two_factor_browser.WHEN_TRUSTED IS 'timestamp of when the browser was last trusted';



CREATE UNIQUE INDEX two_factor_browser_pk ON two_factor_browser(UUID);


CREATE INDEX two_factor_browser_user_id_idx ON two_factor_browser(USER_UUID);

CREATE INDEX tf_browser_delete_idx ON two_factor_browser(DELETED_ON);


CREATE UNIQUE INDEX two_factor_browser_cook_idx ON two_factor_browser(BROWSER_TRUSTED_UUID);



ALTER TABLE two_factor_browser ADD (
  CONSTRAINT two_factor_browser_pk
  PRIMARY KEY
  (UUID)
  USING INDEX two_factor_browser_pk);


ALTER TABLE two_factor_browser ADD (
  CONSTRAINT two_factor_browser_r01 
  FOREIGN KEY (USER_UUID) 
  REFERENCES TWO_FACTOR_USER (UUID));

  
  
  
  
  
  
  
  
CREATE TABLE TWO_FACTOR_DAEMON_LOG
(
  UUID               VARCHAR2(40 CHAR)          NOT NULL,
  DAEMON_NAME        VARCHAR2(40 CHAR)          NOT NULL,
  STATUS             VARCHAR2(40 CHAR)          NOT NULL,
  THE_TIMESTAMP      INTEGER                    NOT NULL,
  VERSION_NUMBER     INTEGER,
  STARTED_TIME       VARCHAR2(40 CHAR),
  ENDED_TIME         VARCHAR2(40 CHAR),
  MILLIS             INTEGER,
  RECORDS_PROCESSED  INTEGER,
  DELETED_ON         INTEGER,
  SERVER_NAME        VARCHAR2(100 CHAR)          NOT NULL,
  LAST_UPDATED       INTEGER                     NOT NULL,
  PROCESS_ID         VARCHAR2(100 CHAR)          ,
  DETAILS            VARCHAR2(2000 CHAR)
);

COMMENT ON TABLE TWO_FACTOR_DAEMON_LOG IS 'log of daemon processing in two factor';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.UUID IS 'uuid of this record';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.DAEMON_NAME IS 'name of the daemon';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.STATUS IS 'if success or error';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.THE_TIMESTAMP IS 'timestamp this record was inserted or updated';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.VERSION_NUMBER IS 'for hibernate, though not really used since only inserting';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.STARTED_TIME IS 'string of starting time of daemon';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.ENDED_TIME IS 'string of ended time of daemon';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.MILLIS IS 'millis the daemon took to process';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.RECORDS_PROCESSED IS 'number of records processed';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.DELETED_ON IS 'millis since 1970 that this row was deleted';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.SERVER_NAME IS 'server name the daemon ran on';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.PROCESS_ID IS 'process id of the daemon';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.LAST_UPDATED IS 'millis since 1970 that this record has been updated';

COMMENT ON COLUMN TWO_FACTOR_DAEMON_LOG.DETAILS IS 'details of the daemon log';

CREATE INDEX TF_DAEMON_LOG_DELETE_IDX ON TWO_FACTOR_DAEMON_LOG
(DELETED_ON);


CREATE INDEX TWO_FACTOR_DAEMON_LOG_NAME_IDX ON TWO_FACTOR_DAEMON_LOG
(DAEMON_NAME, STATUS, ENDED_TIME);


CREATE UNIQUE INDEX TWO_FACTOR_DAEMON_LOG_PK ON TWO_FACTOR_DAEMON_LOG
(UUID);


ALTER TABLE TWO_FACTOR_DAEMON_LOG ADD (
  CONSTRAINT TWO_FACTOR_DAEMON_LOG_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_DAEMON_LOG_PK);

  
  
  
  
  
  
  
  
CREATE TABLE TWO_FACTOR_IP_ADDRESS
(
  UUID                   VARCHAR2(40 CHAR),
  IP_ADDRESS             VARCHAR2(45 CHAR)      NOT NULL,
  DOMAIN_NAME            VARCHAR2(200 CHAR),
  LOOKED_UP_DOMAIN_NAME  VARCHAR2(1 CHAR)       NOT NULL,
  DELETED_ON             INTEGER,
  LAST_UPDATED    INTEGER                       NOT NULL,
  VERSION_NUMBER         INTEGER                NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_IP_ADDRESS IS 'row for each source ip address used in two factor';

COMMENT ON COLUMN TWO_FACTOR_IP_ADDRESS.UUID IS 'this is the primary key';

COMMENT ON COLUMN TWO_FACTOR_IP_ADDRESS.IP_ADDRESS IS 'ip address (ipv4 or ipv6) of the source';

COMMENT ON COLUMN TWO_FACTOR_IP_ADDRESS.DOMAIN_NAME IS 'after doing a reverse lookup, domain name if one found';

COMMENT ON COLUMN TWO_FACTOR_IP_ADDRESS.LOOKED_UP_DOMAIN_NAME IS 'T or F if this IP address has been looked up';

COMMENT ON COLUMN TWO_FACTOR_IP_ADDRESS.DELETED_ON IS 'millis since 1970 since this record has been deleted or null for not deleted';

COMMENT ON COLUMN TWO_FACTOR_IP_ADDRESS.VERSION_NUMBER IS 'increments each time the record is stored, for optimistic locking';

COMMENT ON COLUMN TWO_FACTOR_IP_ADDRESS.LAST_UPDATED IS 'millis since 1970 that this record has been updated';



CREATE UNIQUE INDEX TWO_FACTOR_IP_ADDRESS_PK ON TWO_FACTOR_IP_ADDRESS
(UUID);


CREATE UNIQUE INDEX TWO_FACTOR_IP_ADDRESS_U01 ON TWO_FACTOR_IP_ADDRESS
(IP_ADDRESS);


CREATE INDEX TWO_FACTOR_IP_DOMAIN_NAME_IDX ON TWO_FACTOR_IP_ADDRESS
(DOMAIN_NAME);


ALTER TABLE TWO_FACTOR_IP_ADDRESS ADD (
  CONSTRAINT TWO_FACTOR_IP_ADDRESS_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_IP_ADDRESS_PK);

ALTER TABLE TWO_FACTOR_IP_ADDRESS ADD (
  CONSTRAINT TWO_FACTOR_IP_ADDRESS_U01
  UNIQUE (IP_ADDRESS)
  USING INDEX TWO_FACTOR_IP_ADDRESS_U01);

  
  
  
  
  
  
CREATE TABLE TWO_FACTOR_SERVICE_PROVIDER
(
  UUID                   VARCHAR2(40 CHAR)      NOT NULL,
  SERVICE_PROVIDER_ID    VARCHAR2(100 CHAR)     NOT NULL,
  SERVICE_PROVIDER_NAME  VARCHAR2(100 CHAR),
  DELETED_ON             INTEGER,
  LAST_UPDATED           INTEGER                NOT NULL,
  VERSION_NUMBER         INTEGER                NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_SERVICE_PROVIDER IS 'list of service providers (applications that are requiring the authentication)';

COMMENT ON COLUMN TWO_FACTOR_SERVICE_PROVIDER.UUID IS 'unique identifier';

COMMENT ON COLUMN TWO_FACTOR_SERVICE_PROVIDER.SERVICE_PROVIDER_ID IS 'id of the service provider sent from authn system';

COMMENT ON COLUMN TWO_FACTOR_SERVICE_PROVIDER.SERVICE_PROVIDER_NAME IS 'name of the service provider sent from the authn system';

COMMENT ON COLUMN TWO_FACTOR_SERVICE_PROVIDER.DELETED_ON IS 'millis since 1970 since this record has been deleted or null for not deleted';

COMMENT ON COLUMN TWO_FACTOR_SERVICE_PROVIDER.VERSION_NUMBER IS 'increments each time this record was changed, for optimistic locking';

COMMENT ON COLUMN TWO_FACTOR_SERVICE_PROVIDER.LAST_UPDATED IS 'millis since 1970 that this record has been updated';



CREATE UNIQUE INDEX TWO_FACTOR_SERVICE_PROVIDER_PK ON TWO_FACTOR_SERVICE_PROVIDER
(UUID);


CREATE UNIQUE INDEX TWO_FACTOR_SP_ID_IDX ON TWO_FACTOR_SERVICE_PROVIDER
(SERVICE_PROVIDER_ID);


ALTER TABLE TWO_FACTOR_SERVICE_PROVIDER ADD (
  CONSTRAINT TWO_FACTOR_SERVICE_PROVIDER_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_SERVICE_PROVIDER_PK);

  
  
  
  
  
  
CREATE TABLE TWO_FACTOR_USER_AGENT
(
  UUID              VARCHAR2(40 CHAR)           NOT NULL,
  USER_AGENT        VARCHAR2(200 CHAR)          NOT NULL,
  BROWSER           VARCHAR2(30 CHAR),
  OPERATING_SYSTEM  VARCHAR2(30 CHAR),
  MOBILE            VARCHAR2(1 CHAR),
  DELETED_ON             INTEGER,
  LAST_UPDATED    INTEGER                       NOT NULL,
  VERSION_NUMBER    INTEGER                     NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_USER_AGENT IS 'list of user agents from browsers';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.UUID IS 'this is the primary key for the table, uuid';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.USER_AGENT IS 'user agent sent from the browser';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.BROWSER IS 'assumed browser from user agent';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.OPERATING_SYSTEM IS 'assumed OS from the user agent';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.MOBILE IS 'if this is a mobile device';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.VERSION_NUMBER IS 'increments each time the record is stored for optimistic locking';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.DELETED_ON IS 'millis since 1970 since this record has been deleted or null for not deleted';

COMMENT ON COLUMN TWO_FACTOR_USER_AGENT.LAST_UPDATED IS 'millis since 1970 that this record has been updated';



CREATE UNIQUE INDEX TWO_FACTOR_AGENT_AGENT_IDX ON TWO_FACTOR_USER_AGENT
(USER_AGENT);


CREATE UNIQUE INDEX TWO_FACTOR_USER_AGENT_PK ON TWO_FACTOR_USER_AGENT
(UUID);


ALTER TABLE TWO_FACTOR_USER_AGENT ADD (
  CONSTRAINT TWO_FACTOR_USER_AGENT_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_USER_AGENT_PK);

ALTER TABLE TWO_FACTOR_USER_AGENT ADD (
  CONSTRAINT TWO_FACTOR_USER_AGENT_U01
  UNIQUE (USER_AGENT)
  USING INDEX TWO_FACTOR_AGENT_AGENT_IDX);

  
  
  
  
  
  
CREATE TABLE TWO_FACTOR_USER_ATTR
(
  ATTRIBUTE_NAME           VARCHAR2(30 CHAR)    NOT NULL,
  USER_UUID                VARCHAR2(40 CHAR)    NOT NULL,
  LAST_UPDATED             INTEGER              NOT NULL,
  VERSION_NUMBER           INTEGER              NOT NULL,
  ATTRIBUTE_VALUE_STRING   VARCHAR2(400 CHAR),
  ATTRIBUTE_VALUE_INTEGER  INTEGER,
  DELETED_ON               INTEGER,
  ENCRYPTION_TIMESTAMP     INTEGER,
  UUID                     VARCHAR2(40 CHAR)    NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_USER_ATTR IS 'user field for if passes have been given to the user, this is the last index';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.ATTRIBUTE_NAME IS 'name of the attribute for this user: opted_in, sequential_pass_given_to_user, sequential_pass_index, two_factor_secret, two_factor_secret_temp';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.USER_UUID IS 'foreign key to two_factor_user';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.LAST_UPDATED IS 'when this row was last updated';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.VERSION_NUMBER IS 'hibernate version number to make sure two updates dont happen at once';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.ATTRIBUTE_VALUE_STRING IS 'if this is a string or boolean attribute, this is the value';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.ATTRIBUTE_VALUE_INTEGER IS 'if this is an integer attribute, this is the value';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.DELETED_ON IS 'if this row needs to be deleted, set a delete date for a week, then actually delete it';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.ENCRYPTION_TIMESTAMP IS 'timestamp used for encrypting the data if applicable';

COMMENT ON COLUMN TWO_FACTOR_USER_ATTR.UUID IS 'primary key';



CREATE UNIQUE INDEX TWO_FACTOR_USER_ATTR_PK ON TWO_FACTOR_USER_ATTR
(UUID);


CREATE UNIQUE INDEX TWO_FACTOR_USER_ATTR_U01 ON TWO_FACTOR_USER_ATTR
(ATTRIBUTE_NAME, USER_UUID);


CREATE INDEX TWO_FACTOR_USER_ATTR_USERIDX ON TWO_FACTOR_USER_ATTR
(USER_UUID);


ALTER TABLE TWO_FACTOR_USER_ATTR ADD (
  CONSTRAINT TWO_FACTOR_USER_ATTR_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_USER_ATTR_PK);

ALTER TABLE TWO_FACTOR_USER_ATTR ADD (
  CONSTRAINT TWO_FACTOR_USER_ATTR_U01
  UNIQUE (ATTRIBUTE_NAME, USER_UUID)
  USING INDEX TWO_FACTOR_USER_ATTR_U01);


ALTER TABLE TWO_FACTOR_USER_ATTR ADD (
  CONSTRAINT TWO_FACTOR_USER_ATTR_R01 
  FOREIGN KEY (USER_UUID) 
  REFERENCES TWO_FACTOR_USER (UUID));

  
  
  
  
  
  
CREATE TABLE TWO_FACTOR_AUDIT
(
  UUID                   VARCHAR2(40 CHAR)      NOT NULL,
  USER_UUID              VARCHAR2(40 CHAR),
  BROWSER_UUID           VARCHAR2(40 CHAR),
  THE_TIMESTAMP          INTEGER                NOT NULL,
  ACTION                 VARCHAR2(30 CHAR)      NOT NULL,
  DESCRIPTION            VARCHAR2(1000 CHAR),
  IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
  SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
  USER_AGENT_UUID        VARCHAR2(40 CHAR),
  VERSION_NUMBER         INTEGER,
  DELETED_ON             INTEGER,
  USER_UUID_USING_APP    VARCHAR2(40 CHAR)
);

COMMENT ON TABLE TWO_FACTOR_AUDIT IS 'entry for every audit even that happens in the system';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.UUID IS 'unique id of the row';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.USER_UUID IS 'foreign key to the user';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.BROWSER_UUID IS 'foreign key to the browser';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.THE_TIMESTAMP IS 'timestamp of this record';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.ACTION IS 'action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, INVALIDATE_PASSWORDS, GENERATE_PASSWORDS';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.DESCRIPTION IS 'if a description is needed, this is more info';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.IP_ADDRESS_UUID IS 'foreign key to the ip address';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.SERVICE_PROVIDER_UUID IS 'foreign key to the service provider';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.USER_AGENT_UUID IS 'foreign key to the user agent';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.VERSION_NUMBER IS 'increments each time the record is stored, for optimistic locking';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.DELETED_ON IS 'when this row was deleted millis since 1970, will be actually deleted in a week';

COMMENT ON COLUMN TWO_FACTOR_AUDIT.USER_UUID_USING_APP IS 'the user uuid (foreign key to user table) of the user using the app';



CREATE INDEX TF_AUDIT_DELETE_IDX ON TWO_FACTOR_AUDIT
(DELETED_ON);


CREATE INDEX TWO_FACTOR_AUDIT_ACTION_IDX ON TWO_FACTOR_AUDIT
(ACTION);


CREATE INDEX TWO_FACTOR_AUDIT_AGENT_IDX ON TWO_FACTOR_AUDIT
(USER_AGENT_UUID);


CREATE INDEX TWO_FACTOR_AUDIT_BROWSER_IDX ON TWO_FACTOR_AUDIT
(BROWSER_UUID);


CREATE INDEX TWO_FACTOR_AUDIT_IP_ADDR_IDX ON TWO_FACTOR_AUDIT
(IP_ADDRESS_UUID);


CREATE UNIQUE INDEX TWO_FACTOR_AUDIT_PK ON TWO_FACTOR_AUDIT
(UUID);


CREATE INDEX TWO_FACTOR_AUDIT_SP_IDX ON TWO_FACTOR_AUDIT
(SERVICE_PROVIDER_UUID);


CREATE INDEX TWO_FACTOR_AUDIT_USER_IDX ON TWO_FACTOR_AUDIT
(USER_UUID);


CREATE INDEX TWO_FACTOR_AUDIT_USER_USE_IDX ON TWO_FACTOR_AUDIT
(USER_UUID_USING_APP);


ALTER TABLE TWO_FACTOR_AUDIT ADD (
  CONSTRAINT TWO_FACTOR_AUDIT_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_AUDIT_PK);


ALTER TABLE TWO_FACTOR_AUDIT ADD (
  CONSTRAINT TWO_FACTOR_AUDIT_R01 
  FOREIGN KEY (USER_UUID) 
  REFERENCES TWO_FACTOR_USER (UUID));

ALTER TABLE TWO_FACTOR_AUDIT ADD (
  CONSTRAINT TWO_FACTOR_AUDIT_R02 
  FOREIGN KEY (BROWSER_UUID) 
  REFERENCES two_factor_browser (UUID));

ALTER TABLE TWO_FACTOR_AUDIT ADD (
  CONSTRAINT TWO_FACTOR_AUDIT_R03 
  FOREIGN KEY (SERVICE_PROVIDER_UUID) 
  REFERENCES TWO_FACTOR_SERVICE_PROVIDER (UUID));

ALTER TABLE TWO_FACTOR_AUDIT ADD (
  CONSTRAINT TWO_FACTOR_AUDIT_R04 
  FOREIGN KEY (USER_AGENT_UUID) 
  REFERENCES TWO_FACTOR_USER_AGENT (UUID));

ALTER TABLE TWO_FACTOR_AUDIT ADD (
  CONSTRAINT TWO_FACTOR_AUDIT_R05 
  FOREIGN KEY (IP_ADDRESS_UUID) 
  REFERENCES TWO_FACTOR_IP_ADDRESS (UUID));

  
  
    
/* Formatted on 7/25/2013 9:45:49 PM (QP5 v5.163.1008.3004) */
CREATE OR REPLACE FORCE VIEW TWO_FACTOR_USER_V
(
   LOGINID,
   UUID,
   OPTED_IN,
   SEQUENTIAL_PASS_INDEX,
   SEQUENTIAL_PASS_GIVEN_TO_USER,
   TWO_FACTOR_SECRET_ABBR,
   TWO_FACTOR_SECRET_TEMP_ABBR,
   LAST_TOTP_TIMESTAMP_USED,
   LAST_TOTP60_TIMESTAMP_USED,
   PHONE0,
   PHONE_IS_TEXT0,
   PHONE_IS_VOICE0,
   PHONE1,
   PHONE_IS_TEXT1,
   PHONE_IS_VOICE1,
   PHONE2,
   PHONE_IS_TEXT2,
   PHONE_IS_VOICE2,
   COLLEAGUE_USER_UUID0,
   COLLEAGUE_LOGINID0,
   COLLEAGUE_USER_UUID1,
   COLLEAGUE_LOGINID1,
   COLLEAGUE_USER_UUID2,
   COLLEAGUE_LOGINID2,
   COLLEAGUE_USER_UUID3,
   COLLEAGUE_LOGINID3,
   COLLEAGUE_USER_UUID4,
   COLLEAGUE_LOGINID4,
   EMAIL0,
   DATE_INVITED_COLLEAGUES,
   DATE_INVITED_COLLEAGUES_DATE,
   PHONE_CODE_ENCRYPTED,
   DATE_PHONE_CODE_SENT,
   DATE_PHONE_CODE_SENT_DATE
)
AS
   SELECT tfu.loginid,
          TFU.UUID,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'opted_in')
             AS opted_in,
          (SELECT TFUA.ATTRIBUTE_VALUE_INTEGER
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'sequential_pass_index')
             AS sequential_pass_index,
          (SELECT TFUA.ATTRIBUTE_VALUE_INTEGER
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'sequential_pass_given_to_user')
             AS sequential_pass_given_to_user,
          (SELECT DECODE (
                     TFUA.ATTRIBUTE_VALUE_String,
                     NULL, NULL,
                     SUBSTR (TFUA.ATTRIBUTE_VALUE_String, 1, 3) || '...')
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'two_factor_secret')
             AS two_factor_secret_abbr,
          (SELECT DECODE (
                     TFUA.ATTRIBUTE_VALUE_String,
                     NULL, NULL,
                     SUBSTR (TFUA.ATTRIBUTE_VALUE_String, 1, 3) || '...')
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'two_factor_secret_temp')
             AS two_factor_secret_temp_abbr,
          (SELECT TFUA.ATTRIBUTE_VALUE_Integer
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'last_totp_timestamp_used')
             AS last_totp_timestamp_used,
          (SELECT TFUA.ATTRIBUTE_VALUE_Integer
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'last_totp60_timestamp_used')
             AS last_totp60_timestamp_used,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone0')
             AS phone0,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone_is_text0')
             AS phone_is_text0,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone_is_voice0')
             AS phone_is_voice0,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone1')
             AS phone1,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone_is_text1')
             AS phone_is_text1,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone_is_voice1')
             AS phone_is_voice1,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone2')
             AS phone2,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone_is_text2')
             AS phone_is_text2,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone_is_voice2')
             AS phone_is_voice2,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid0')
             AS colleague_user_uuid0,
          (SELECT TFU1.loginid
             FROM two_factor_user_attr tfua, two_factor_user tfu1
            WHERE     TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid0'
                  AND tfua.attribute_value_string = tfu1.uuid)
             AS colleague_loginid0,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid1')
             AS colleague_user_uuid1,
          (SELECT TFU1.loginid
             FROM two_factor_user_attr tfua, two_factor_user tfu1
            WHERE     TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid1'
                  AND tfua.attribute_value_string = tfu1.uuid)
             AS colleague_loginid1,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid2')
             AS colleague_user_uuid2,
          (SELECT TFU1.loginid
             FROM two_factor_user_attr tfua, two_factor_user tfu1
            WHERE     TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid2'
                  AND tfua.attribute_value_string = tfu1.uuid)
             AS colleague_loginid2,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid3')
             AS colleague_user_uuid3,
          (SELECT TFU1.loginid
             FROM two_factor_user_attr tfua, two_factor_user tfu1
            WHERE     TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid3'
                  AND tfua.attribute_value_string = tfu1.uuid)
             AS colleague_loginid3,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid4')
             AS colleague_user_uuid4,
          (SELECT TFU1.loginid
             FROM two_factor_user_attr tfua, two_factor_user tfu1
            WHERE     TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'colleague_user_uuid4'
                  AND tfua.attribute_value_string = tfu1.uuid)
             AS colleague_loginid4,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'email0')
             AS email0,
          (SELECT TFUA.ATTRIBUTE_VALUE_INTEGER
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'date_invited_colleagues')
             AS date_invited_colleagues,
          (SELECT CASE
                     WHEN TFUA.ATTRIBUTE_VALUE_INTEGER = 0
                     THEN
                        NULL
                     ELSE
                        DATE '1970-01-01'
                        + TFUA.ATTRIBUTE_VALUE_INTEGER / 1000 / 60 / 60 / 24
                  END
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'date_invited_colleagues')
             AS date_invited_colleagues_date,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'phone_code_encrypted')
             AS phone_code_encrypted,
          (SELECT TFUA.ATTRIBUTE_VALUE_INTEGER
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'date_phone_code_sent')
             AS date_phone_code_sent,
          (SELECT CASE
                     WHEN TFUA.ATTRIBUTE_VALUE_INTEGER = 0
                     THEN
                        NULL
                     ELSE
                        DATE '1970-01-01'
                        + TFUA.ATTRIBUTE_VALUE_INTEGER / 1000 / 60 / 60 / 24
                  END
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'date_phone_code_sent')
             AS date_phone_code_sent_date
     FROM two_factor_user tfu;
COMMENT ON TABLE TWO_FACTOR_USER_V IS 'user and attributes of user in one view mainly for auditing purposes';

COMMENT ON COLUMN TWO_FACTOR_USER_V.LOGINID IS 'loginid that the user used to login to the system';

COMMENT ON COLUMN TWO_FACTOR_USER_V.UUID IS 'uuid of the user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.OPTED_IN IS 'T or F if the user has opted in or null if not set';

COMMENT ON COLUMN TWO_FACTOR_USER_V.SEQUENTIAL_PASS_INDEX IS 'index starting with 1 of the sequential passes';

COMMENT ON COLUMN TWO_FACTOR_USER_V.SEQUENTIAL_PASS_GIVEN_TO_USER IS 'lsat index given to the user as scratch codes';

COMMENT ON COLUMN TWO_FACTOR_USER_V.TWO_FACTOR_SECRET_ABBR IS 'abbreviated two factor secret assigned to the user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.TWO_FACTOR_SECRET_TEMP_ABBR IS 'abbreviated two factor secret temp which is assigned to the user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.LAST_TOTP_TIMESTAMP_USED IS 'the last timestamp used by totp (30) so we dont accept the same pass twice';

COMMENT ON COLUMN TWO_FACTOR_USER_V.LAST_TOTP60_TIMESTAMP_USED IS 'the last timestamp used by totp (60) so we dont accept the same pass twice';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE0 IS 'phone to opt out 0';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE_IS_TEXT0 IS 'T if phone 0 is available for texting';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE_IS_VOICE0 IS 'T if phone 0 is available for voice calls';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE1 IS 'phone to opt out 1';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE_IS_TEXT1 IS 'T if phone 1 is available for texting';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE_IS_VOICE1 IS 'T if phone 1 is available for voice calls';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE2 IS 'phone to opt out 2';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE_IS_TEXT2 IS 'T if phone 2 is available for texting';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE_IS_VOICE2 IS 'T if phone 2 is available for voice calls';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_USER_UUID0 IS 'userId 0 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_LOGINID0 IS 'loginid 0 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_USER_UUID1 IS 'userId 1 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_LOGINID1 IS 'loginid 1 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_USER_UUID2 IS 'userId 2 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_LOGINID2 IS 'loginid 2 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_USER_UUID3 IS 'userId 3 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_LOGINID3 IS 'loginid 3 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_USER_UUID4 IS 'userId 4 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.COLLEAGUE_LOGINID4 IS 'loginid 4 of the colleague who can unlock this user';

COMMENT ON COLUMN TWO_FACTOR_USER_V.EMAIL0 IS 'date value for when the phone code was sent';

COMMENT ON COLUMN TWO_FACTOR_USER_V.DATE_INVITED_COLLEAGUES IS 'date the user invited colleagues to unlock account as int millis from 1970';

COMMENT ON COLUMN TWO_FACTOR_USER_V.DATE_INVITED_COLLEAGUES_DATE IS 'date the user invited colleagues to unlock account';

COMMENT ON COLUMN TWO_FACTOR_USER_V.PHONE_CODE_ENCRYPTED IS 'six digit phone code for unlocking account';

COMMENT ON COLUMN TWO_FACTOR_USER_V.DATE_PHONE_CODE_SENT IS 'millis since 1970 that the phone code was sent';





  
  
  
  
/* Formatted on 3/12/2013 9:57:42 AM (QP5 v5.163.1008.3004) */
CREATE OR REPLACE FORCE VIEW TWO_FACTOR_AUDIT_V
(
   THE_TIMESTAMP_DATE,
   ACTION,
   LOGINID,
   user_using_LOGINID,
   TRUSTED_BROWSER,
   IP_ADDRESS,
   USER_AGENT_OPERATING_SYSTEM,
   USER_AGENT_BROWSER,
   USER_AGENT_MOBILE,
   SERVICE_PROVIDER_ID,
   SERVICE_PROVIDER_NAME,
   DESCRIPTION,
   DOMAIN_NAME,
   WHEN_BROWSER_TRUSTED_DATE,
   USER_AGENT,
   UUID,
   THE_TIMESTAMP,
   WHEN_BROWSER_TRUSTED,
   USER_UUID,
   BROWSER_UUID,
   IP_ADDRESS_UUID,
   SERVICE_PROVIDER_UUID,
   USER_AGENT_UUID,
   USER_UUID_USING_APP
)
AS
   SELECT CASE
             WHEN tfa.the_timestamp = 0 THEN NULL
             ELSE DATE '1970-01-01' + tfa.the_timestamp / 1000 / 60 / 60 / 24
          END
             AS the_timestamp_date,
          tfa.action,
          (SELECT tfuv.loginid
             FROM two_factor_user_v tfuv
            WHERE tfuv.uuid = tfa.user_uuid)
             AS loginid,
          (SELECT tfuv.loginid
             FROM two_factor_user_v tfuv
            WHERE tfuv.uuid = tfa.USER_UUID_USING_APP)
             AS user_using_loginid,
          (SELECT TFB.TRUSTED_BROWSER
             FROM two_factor_browser tfb
            WHERE tfb.uuid = tfa.browser_uuid)
             AS trusted_browser,
          (SELECT TFIA.IP_ADDRESS
             FROM two_factor_ip_address tfia
            WHERE tfia.uuid = TFA.IP_ADDRESS_UUID)
             AS ip_address,
          (SELECT TFUA.OPERATING_SYSTEM
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent_operating_system,
          (SELECT TFUA.BROWSER
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent_browser,
          (SELECT TFUA.MOBILE
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent_mobile,
          (SELECT TFSP.SERVICE_PROVIDER_ID
             FROM two_factor_service_provider tfsp
            WHERE tfsp.uuid = tfa.service_provider_uuid)
             AS service_provider_id,
          (SELECT TFSP.SERVICE_PROVIDER_name
             FROM two_factor_service_provider tfsp
            WHERE tfsp.uuid = tfa.service_provider_uuid)
             AS service_provider_name,
          TFA.DESCRIPTION,
          (SELECT TFIA.DOMAIN_NAME
             FROM two_factor_ip_address tfia
            WHERE tfia.uuid = TFA.IP_ADDRESS_UUID)
             AS domain_name,
          (SELECT CASE
                     WHEN tfb.when_trusted = 0
                     THEN
                        NULL
                     ELSE
                        DATE '1970-01-01'
                        + tfb.when_trusted / 1000 / 60 / 60 / 24
                  END
             FROM two_factor_browser tfb
            WHERE tfb.uuid = tfa.browser_uuid)
             AS when_browser_trusted_date,
          (SELECT TFUA.user_agent
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent,
          TFA.UUID,
          tfa.the_timestamp,
          (SELECT tfb.when_trusted
             FROM two_factor_browser tfb
            WHERE tfb.uuid = tfa.browser_uuid)
             AS when_browser_trusted,
          TFA.USER_UUID,
          TFA.BROWSER_UUID,
          TFA.IP_ADDRESS_UUID,
          TFA.SERVICE_PROVIDER_UUID,
          TFA.USER_AGENT_UUID,
          tfa.USER_UUID_USING_APP
     FROM two_factor_audit tfa
    WHERE TFA.DELETED_ON IS NULL;
COMMENT ON TABLE TWO_FACTOR_AUDIT_V IS 'view on audit records to make them more human readable';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.THE_TIMESTAMP_DATE IS 'date of when it happened';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.ACTION IS 'action enum';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.LOGINID IS 'loginid of user who performed it';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.TRUSTED_BROWSER IS 'if this is a trusted  browser (might not be in sync)';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.IP_ADDRESS IS 'ip address of user who performed it';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.USER_AGENT_OPERATING_SYSTEM IS 'user agent operating system of user who performed it (might not be accurate)';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.USER_AGENT_BROWSER IS 'user agent browser of user who performed it (might not be accurate)';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.USER_AGENT_MOBILE IS 'T or F if the browser of the user who performed it is mobile (might not be accurate)';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.SERVICE_PROVIDER_ID IS 'service provider id of the affected service provider';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.SERVICE_PROVIDER_NAME IS 'service provider name of the affected service provider';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.DESCRIPTION IS 'description of the audit record if applicable';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.DOMAIN_NAME IS 'domain name of the ip address of the user who performed it';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.WHEN_BROWSER_TRUSTED_DATE IS 'when the browser was last trusted';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.USER_AGENT IS 'the user agent that performed the action';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.UUID IS 'the audit uuid';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.THE_TIMESTAMP IS 'date of when it happened in millis from 1970';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.WHEN_BROWSER_TRUSTED IS 'when the browser was last trusted in millis since 1970';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.USER_UUID IS 'uuid of the user who did the action';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.BROWSER_UUID IS 'uuid of the browser which did the action';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.IP_ADDRESS_UUID IS 'ip address that did the action';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.SERVICE_PROVIDER_UUID IS 'uuid of the service provider involved in the action';

COMMENT ON COLUMN TWO_FACTOR_AUDIT_V.USER_AGENT_UUID IS 'uuid of the user agent involved in the action';
 comment on column two_factor_audit_v.USER_UUID_USING_APP is 'uuid of the user using the application';
 comment on column two_factor_audit_v.user_using_LOGINID is 'login id of the user using the application';
 
 
CREATE OR REPLACE FORCE VIEW two_factor_browser_v
(
           loginid,
           browser_trusted_uuid_hashed,
           last_updated_date,
           TRUSTED_BROWSER,
           when_trusted_date,
           USER_UUID,
           UUID,
           LAST_UPDATED,
           WHEN_TRUSTED
)
AS
   SELECT 
          (SELECT tfu.loginid
             FROM two_factor_user tfu
            WHERE TFU.UUID = TFB.USER_UUID)
             AS loginid,
           TFB.BROWSER_TRUSTED_UUID as browser_trusted_uuid_hashed,
           CASE
             WHEN tfb.last_updated = 0 or tfb.last_updated is null THEN NULL
             ELSE DATE '1970-01-01' + tfb.last_updated / 1000 / 60 / 60 / 24
           END as last_updated_date,
           TFB.TRUSTED_BROWSER,
           CASE
             WHEN TFB.WHEN_TRUSTED = 0 or TFB.WHEN_TRUSTED is null THEN NULL
             ELSE DATE '1970-01-01' + tfb.when_trusted / 1000 / 60 / 60 / 24
           END
             AS when_trusted_date,
           TFB.USER_UUID,
           TFB.UUID,
           TFB.LAST_UPDATED,
           TFB.WHEN_TRUSTED
     FROM two_factor_browser tfb
    WHERE TFb.DELETED_ON IS NULL;


COMMENT ON TABLE two_factor_browser_v IS 'view on browser records';
COMMENT ON COLUMN two_factor_browser_v.loginid IS 'subject id for user';
COMMENT ON COLUMN two_factor_browser_v.browser_trusted_uuid_hashed IS 'hash of browser trusted uuid';
COMMENT ON COLUMN two_factor_browser_v.last_updated_date IS 'date representation of last updated for record';
COMMENT ON COLUMN two_factor_browser_v.trusted_browser IS 'T or F if this is a trusted browser';
COMMENT ON COLUMN two_factor_browser_v.when_trusted_date IS 'date that this browser was trusted';
COMMENT ON COLUMN two_factor_browser_v.user_uuid IS 'uuid of the user record';
COMMENT ON COLUMN two_factor_browser_v.uuid IS 'uuid of this record';
COMMENT ON COLUMN two_factor_browser_v.last_updated IS 'number of millis since 1970 since this was updated';
COMMENT ON COLUMN two_factor_browser_v.when_trusted IS 'number of millis since 1970 since this was trusted';

 
 
 
 


 

CREATE TABLE two_factor_sample_source
(
  subject_id                    VARCHAR2(100) NOT NULL,
  net_id                        VARCHAR2(100) NOT NULL,
  name                          VARCHAR2(200) NOT NULL,
  description                   VARCHAR2(200) NOT NULL,
  search_string_lower           VARCHAR2(200),
  email                         VARCHAR2(256),
  active                        VARCHAR2(256) NOT NULL
);

COMMENT ON TABLE two_factor_sample_source IS 'Sample internet2 subject api table for quick start.  You dont have to use this table, you could use another view or ldap etc.';

COMMENT ON COLUMN two_factor_sample_source.subject_id IS 'opaque id for user of system';

COMMENT ON COLUMN two_factor_sample_source.net_id IS 'net_id of the user that they use to login with';

COMMENT ON COLUMN two_factor_sample_source.name IS 'first and last name of user';

COMMENT ON COLUMN two_factor_sample_source.description IS 'what is shown on the screen for this user';

COMMENT ON COLUMN two_factor_sample_source.search_string_lower IS 'lower case stuff to search on';

COMMENT ON COLUMN two_factor_sample_source.email IS 'email address of user';

COMMENT ON COLUMN two_factor_sample_source.active IS 'T or F for if the user is active';

CREATE UNIQUE INDEX two_factor_sample_source_idx ON two_factor_sample_source (subject_id);

alter table two_factor_sample_source add (constraint two_factor_sample_source_pk primary key (subject_id) using index two_factor_sample_source_idx);

CREATE UNIQUE INDEX two_factor_sample_source_u01 ON two_factor_sample_source (net_id);

insert into two_factor_sample_source (subject_id, net_id, name, description, search_string_lower, email, active) 
  values ('12345678', 'abc', 'JohnSmith Sample', 'JohnSmith Sample (abc, 12345678) (active) - Faculty - English (also: Staff)', '12345678 abc johnsmith sample', 'abc@nowhere.whatever', 'T');
insert into two_factor_sample_source (subject_id, net_id, name, description, search_string_lower, email, active) 
  values ('12345679', 'abd', 'GeorgeJones Sample', 'GeorgeJones Sample (abd, 12345679) (active) - Staff - IT - Developer (also: Student)', '12345678 abd georgejones sample', 'abd@nowhere.whatever', 'T');
insert into two_factor_sample_source (subject_id, net_id, name, description, search_string_lower, email, active) 
  values ('12345680', 'abe', 'JennyGreen Sample', 'JennyGreen Sample (abe, 123456780) (active) - Student - SAS - Chemistry', '12345680 abe jennygreen sample', 'abe@nowhere.whatever', 'T');
insert into two_factor_sample_source (subject_id, net_id, name, description, search_string_lower, email, active) 
  values ('12345681', 'abf', 'SallyJohnson Sample', 'SallyJohnson Sample (abf, 12345681) (active) - Staff - Provost Office - Director', '12345681 abf sallyjohnson sample', 'abf@nowhere.whatever', 'T');
insert into two_factor_sample_source (subject_id, net_id, name, description, search_string_lower, email, active) 
  values ('12345682', 'abg', 'DavidJackson Sample', 'DavidJackson Sample (abg, 12345683) (NOT_ACTIVE) - Alumni', '12345682 abg davidjackson sample', 'abg@nowhere.whatever', 'F');

commit;

CREATE TABLE TWO_FACTOR_REPORT
(
  UUID                  VARCHAR2(40 CHAR)       NOT NULL,
  LAST_UPDATED          NUMBER                  NOT NULL,
  REPORT_TYPE           VARCHAR2(32 CHAR)       NOT NULL,
  REPORT_NAME_FRIENDLY  VARCHAR2(200 CHAR)      NOT NULL,
  REPORT_NAME_SYSTEM    VARCHAR2(200 CHAR)     NOT NULL,
  VERSION_NUMBER        NUMBER                  NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_REPORT IS 'each report has a row here. users cant access reports on the UI';

COMMENT ON COLUMN TWO_FACTOR_REPORT.UUID IS 'uuid primary key of this row';

COMMENT ON COLUMN TWO_FACTOR_REPORT.LAST_UPDATED IS 'when this row was last updated';

COMMENT ON COLUMN TWO_FACTOR_REPORT.REPORT_TYPE IS 'must be of the TwoFactorReportType enum, e.g. group or rollup';

COMMENT ON COLUMN TWO_FACTOR_REPORT.REPORT_NAME_FRIENDLY IS 'friendly name is included in the report UI';

COMMENT ON COLUMN TWO_FACTOR_REPORT.REPORT_NAME_SYSTEM IS 'some system key on the report which can be used for queries to populate data and should not change';

COMMENT ON COLUMN TWO_FACTOR_REPORT.VERSION_NUMBER IS 'column for DAO optimistic locking';



CREATE UNIQUE INDEX TF_REPORT_NAME_SYSTEM_IDX ON TWO_FACTOR_REPORT (REPORT_NAME_SYSTEM);


CREATE INDEX TF_REPORT_TYPE_IDX ON TWO_FACTOR_REPORT (REPORT_TYPE);


CREATE UNIQUE INDEX TWO_FACTOR_REPORT_PK ON TWO_FACTOR_REPORT (UUID);


ALTER TABLE TWO_FACTOR_REPORT ADD (CONSTRAINT TWO_FACTOR_REPORT_PK
  PRIMARY KEY (UUID) USING INDEX TWO_FACTOR_REPORT_PK);


CREATE TABLE TWO_FACTOR_REPORT_ROLLUP
(
  UUID                VARCHAR2(40 CHAR)         NOT NULL,
  LAST_UPDATED        NUMBER                    NOT NULL,
  PARENT_REPORT_UUID  VARCHAR2(40 CHAR)         NOT NULL,
  CHILD_REPORT_UUID   VARCHAR2(40 CHAR)         NOT NULL,
  VERSION_NUMBER      NUMBER                    NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_REPORT_ROLLUP IS 'Relationship between reports to rollup reports to parents';

COMMENT ON COLUMN TWO_FACTOR_REPORT_ROLLUP.UUID IS 'unique id for the row';

COMMENT ON COLUMN TWO_FACTOR_REPORT_ROLLUP.LAST_UPDATED IS 'millis since 1970 that this record was edited';

COMMENT ON COLUMN TWO_FACTOR_REPORT_ROLLUP.PARENT_REPORT_UUID IS 'report uuid that implies another report';

COMMENT ON COLUMN TWO_FACTOR_REPORT_ROLLUP.CHILD_REPORT_UUID IS 'report uuid that is implied by another report';

COMMENT ON COLUMN TWO_FACTOR_REPORT_ROLLUP.VERSION_NUMBER IS 'incrementing integer for DAO versioning and optimistic locking';


CREATE UNIQUE INDEX TWO_FACTOR_REPORT_ROLLUP_PK ON TWO_FACTOR_REPORT_ROLLUP (UUID);

CREATE UNIQUE INDEX TF_REPORT_ROLLUP_PARENT_IDX ON TWO_FACTOR_REPORT_ROLLUP
(PARENT_REPORT_UUID, CHILD_REPORT_UUID);

CREATE INDEX TF_REPORT_ROLLUP_CHILD_IDX ON TWO_FACTOR_REPORT_ROLLUP (CHILD_REPORT_UUID);

ALTER TABLE TWO_FACTOR_REPORT_ROLLUP ADD (
  CONSTRAINT TWO_FACTOR_REPORT_ROLLUP_PK
  PRIMARY KEY
  (UUID)
  USING INDEX TWO_FACTOR_REPORT_ROLLUP_PK);

ALTER TABLE TWO_FACTOR_REPORT_ROLLUP ADD 
CONSTRAINT TWO_FACTOR_REPORT_ROLLUP_R01
 FOREIGN KEY (PARENT_REPORT_UUID)
 REFERENCES TWO_FACTOR_REPORT (UUID);
 
 ALTER TABLE TWO_FACTOR_REPORT_ROLLUP ADD 
 CONSTRAINT TWO_FACTOR_REPORT_ROLLUP_R02
  FOREIGN KEY (CHILD_REPORT_UUID)
  REFERENCES TWO_FACTOR_REPORT (UUID);
  

CREATE TABLE TWO_FACTOR_REPORT_PRIVILEGE
(
  UUID            VARCHAR2(40 CHAR)             NOT NULL,
  LAST_UPDATED    NUMBER                        NOT NULL,
  VERSION_NUMBER  NUMBER                        NOT NULL,
  USER_UUID       VARCHAR2(40 CHAR)             NOT NULL,
  REPORT_UUID     VARCHAR2(40 CHAR)             NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_REPORT_PRIVILEGE IS 'Privileges of who can read which reports';

COMMENT ON COLUMN TWO_FACTOR_REPORT_PRIVILEGE.UUID IS 'uuid uniquely identifies each row';

COMMENT ON COLUMN TWO_FACTOR_REPORT_PRIVILEGE.LAST_UPDATED IS 'millis since 1970 that this row was last updated';

COMMENT ON COLUMN TWO_FACTOR_REPORT_PRIVILEGE.VERSION_NUMBER IS 'incrementing number used by the DAO for versioning and optimistic locking';

COMMENT ON COLUMN TWO_FACTOR_REPORT_PRIVILEGE.USER_UUID IS 'uuid foreign key to the user table is the user who can access the report';

COMMENT ON COLUMN TWO_FACTOR_REPORT_PRIVILEGE.REPORT_UUID IS 'uuid foreign key to the report table is the report that the user can access';



ALTER TABLE TWO_FACTOR_REPORT_PRIVILEGE ADD (
  CONSTRAINT TWO_FACTOR_REPORT_PRIVILEGE_PK
  PRIMARY KEY
  (UUID));


CREATE INDEX TF_REPORT_PRIVILEGE_USER_IDX ON TWO_FACTOR_REPORT_PRIVILEGE (USER_UUID);


CREATE INDEX TF_REPORT_PRIVILEGE_REPORT_IDX ON TWO_FACTOR_REPORT_PRIVILEGE (REPORT_UUID);

ALTER TABLE TWO_FACTOR_REPORT_PRIVILEGE ADD 
CONSTRAINT TWO_FACTOR_REPORT_PRIVILEGER01
 FOREIGN KEY (USER_UUID)
 REFERENCES TWO_FACTOR_USER (UUID);

ALTER TABLE TWO_FACTOR_REPORT_PRIVILEGE ADD 
CONSTRAINT TWO_FACTOR_REPORT_PRIVILEGER02
 FOREIGN KEY (REPORT_UUID)
 REFERENCES TWO_FACTOR_REPORT (UUID);



  
  
CREATE TABLE TWO_FACTOR_DEVICE_SERIAL
(
  UUID               VARCHAR2(40 CHAR)          NOT NULL,
  LAST_UPDATED       INTEGER                    NOT NULL,
  DELETED_ON         INTEGER,
  VERSION_NUMBER     INTEGER                    NOT NULL,
  USER_UUID          VARCHAR2(40 CHAR),
  WHEN_REGISTERED    INTEGER,
  SERIAL_NUMBER      VARCHAR2(100 CHAR)         NOT NULL,
  TWO_FACTOR_SECRET  VARCHAR2(400 CHAR)         NOT NULL,
  TWO_FACTOR_SECRET_HASH  VARCHAR2(100 CHAR)    NOT NULL
);

COMMENT ON TABLE TWO_FACTOR_DEVICE_SERIAL IS 'This table holds serial numbers and encrypted secrets for fobs.  These serial numbers can be uploaded in the admin console.';
                 
COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.UUID IS 'UUID of the row, primary key';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.LAST_UPDATED IS 'millis since 1970 that this row was last updated';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.DELETED_ON IS 'millis since 1970 that this row was deleted';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.VERSION_NUMBER IS 'hibernate version for optimistic locking';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.USER_UUID IS 'uuid of the user who registered this fob';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.WHEN_REGISTERED IS 'millis since 1970 that this secret was registered';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.SERIAL_NUMBER IS 'serial number on the device';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.TWO_FACTOR_SECRET IS 'encrypted secret for that device';

COMMENT ON COLUMN TWO_FACTOR_DEVICE_SERIAL.TWO_FACTOR_SECRET_HASH IS 'hashed secret so we can see if a secret has been used already';


ALTER TABLE TWO_FACTOR_DEVICE_SERIAL ADD ( CONSTRAINT TWO_FACTOR_DEVICE_SERIAL_PK
  PRIMARY KEY (UUID));

CREATE UNIQUE INDEX TWO_FACTOR_DEV_SER_SER_IDX ON TWO_FACTOR_DEVICE_SERIAL (SERIAL_NUMBER);

CREATE UNIQUE INDEX TWO_FACTOR_DEV_SER_SECRET_IDX ON TWO_FACTOR_DEVICE_SERIAL (TWO_FACTOR_SECRET);

CREATE INDEX TWO_FACTOR_DEV_SER_USER_IDX ON TWO_FACTOR_DEVICE_SERIAL (USER_UUID);
             
ALTER TABLE TWO_FACTOR_DEVICE_SERIAL ADD CONSTRAINT TWO_FACTOR_DEVICE_SERIAL_R01
 FOREIGN KEY (USER_UUID)
 REFERENCES TWO_FACTOR_USER (UUID);

CREATE UNIQUE INDEX TWO_FACTOR_DEV_SER_HASH_IDX ON TWO_FACTOR_DEVICE_SERIAL (TWO_FACTOR_SECRET_HASH);
 