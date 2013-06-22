CREATE TABLE two_factor_user
(
  UUID                          VARCHAR(40 )  NOT NULL comment 'uuid identifies each row in this table',
  LOGINID                       VARCHAR(100 ) NOT NULL comment 'loginid from the apache plugin for the user, generally this is a netid, though could be anything',
  LAST_UPDATED                  BIGINT(20)    NOT NULL comment 'millis since 1970 that this record has been updated',
  VERSION_NUMBER                BIGINT(20)    NOT NULL comment 'increments each time this record is stored, for optimistic locking purposes',
  DELETED_ON                    BIGINT(20)    comment 'If this row is deleted, set a delete date, and delete it later'
);

CREATE UNIQUE INDEX two_factor_user_LOGINID_IDX ON two_factor_user (LOGINID);

Alter table two_factor_user
  add primary key (UUID);

CREATE UNIQUE INDEX TWO_FACTOR_USER_U01 ON two_factor_user
(LOGINID);






CREATE TABLE two_factor_browser
(
  UUID               VARCHAR(40 )          NOT NULL COMMENT 'uuid for this record',
  USER_UUID          VARCHAR(40 )          NOT NULL COMMENT 'foreign key to the user table',
  TRUSTED_BROWSER    VARCHAR(1 )           NOT NULL COMMENT 'T or F if this browser is trusted',
  WHEN_TRUSTED       BIGINT(20) COMMENT 'millis since 1970 when this browser was trusted',
  LAST_UPDATED       BIGINT(20)                    NOT NULL COMMENT 'millis since 1970 when this browser was last updated',
  VERSION_NUMBER     BIGINT(20)                    NOT NULL COMMENT 'increments each time the record is stored, for optimistic locking',
  DELETED_ON         BIGINT(20) comment 'if this row is to be deleted, this is the number of millis since 1970, then a week later it will actually be deleted',
  BROWSER_TRUSTED_UUID  VARCHAR(40)       NOT NULL comment 'the cookie value of the browser, encrypted'
);

Alter table two_factor_browser
  add primary key (UUID);

CREATE UNIQUE INDEX two_factor_browser_U01 ON two_factor_browser
(BROWSER_UUID_HASH);

CREATE unique INDEX two_factor_browser_USER_ID_IDX ON two_factor_browser
(USER_UUID);

CREATE INDEX tf_browser_delete_idx ON two_factor_browser(DELETED_ON);


CREATE UNIQUE INDEX two_factor_browser_cook_idx ON two_factor_browser(BROWSER_TRUSTED_UUID);




ALTER TABLE two_factor_browser ADD (
  CONSTRAINT two_factor_browser_R01 
  FOREIGN KEY (USER_UUID) 
  REFERENCES two_factor_user (UUID));

  
  
  
  
  
CREATE TABLE two_factor_daemon_log
(
  UUID               VARCHAR(40)          NOT NULL comment 'uuid of this record',
  DAEMON_NAME        VARCHAR(40)          NOT NULL comment 'name of the daemon',
  STATUS             VARCHAR(40)          NOT NULL comment 'if success or error',
  THE_TIMESTAMP      BIGINT(20)           NOT NULL comment 'timestamp this record was inserted or updated',
  VERSION_NUMBER     BIGINT(20)                    comment 'for hibernate, though not really used since only inserting',
  STARTED_TIME       VARCHAR(40)                   comment 'string of starting time of daemon',
  ENDED_TIME         VARCHAR(40)                   comment 'string of ended time of daemon',
  MILLIS             BIGINT(20)                    comment 'millis the daemon took to process', 
  RECORDS_PROCESSED  BIGINT(20)                    comment 'number of records processed',
  DELETED_ON         BIGINT(20)                    comment 'millis since 1970 that this row was deleted',
  SERVER_NAME        VARCHAR(100)         NOT NULL comment 'server name the daemon ran on',
  LAST_UPDATED       BIGINT(20)           NOT NULL comment 'millis since 1970 that this record has been updated',
  PROCESS_ID         VARCHAR(100)         NOT NULL comment 'process id of the daemon'
);

alter table two_factor_daemon_log
  add primary key (uuid);

CREATE INDEX TF_DAEMON_LOG_DELETE_IDX ON two_factor_daemon_log
(DELETED_ON);


CREATE INDEX TWO_FACTOR_DAEMON_LOG_NAME_IDX ON two_factor_daemon_log
(DAEMON_NAME);


CREATE UNIQUE INDEX TWO_FACTOR_DAEMON_LOG_PK ON two_factor_daemon_log
(UUID);




CREATE TABLE two_factor_ip_address
(
  UUID                   VARCHAR(40 ) comment 'this is the primary key',
  IP_ADDRESS             VARCHAR(45 )      NOT NULL comment 'ip address (ipv4 or ipv6) of the source',
  DOMAIN_NAME            VARCHAR(80 ) comment 'after doing a reverse lookup, domain name if one found',
  LOOKED_UP_DOMAIN_NAME  VARCHAR(1)       NOT NULL comment 'T or F if this IP address has been looked up',
  DELETED_ON             BIGINT(20)    comment 'If this row is deleted, set a delete date, and delete it later',
  LAST_UPDATED           BIGINT(20)    NOT NULL comment 'millis since 1970 that this record has been updated',
  VERSION_NUMBER         BIGINT(20)                NOT NULL comment 'increments each time the record is stored, for optimistic locking'
);

Alter table two_factor_ip_address
  add primary key (UUID);



CREATE UNIQUE INDEX two_factor_ip_address_U01 ON two_factor_ip_address
(IP_ADDRESS);


CREATE INDEX TWO_FACTOR_IP_DOMAIN_NAME_IDX ON two_factor_ip_address
(DOMAIN_NAME);






  
CREATE TABLE two_factor_service_provider
(
  UUID                   VARCHAR(40)      NOT NULL comment 'unique identifier',
  SERVICE_PROVIDER_ID    VARCHAR(100)     NOT NULL comment 'id of the service provider sent from authn system',
  SERVICE_PROVIDER_NAME  VARCHAR(100) comment 'name of the service provider sent from the authn system',
  DELETED_ON                    BIGINT(20)    comment 'If this row is deleted, set a delete date, and delete it later',
  LAST_UPDATED                  BIGINT(20)    NOT NULL comment 'millis since 1970 that this record has been updated',
  VERSION_NUMBER         BIGINT(20)                NOT NULL comment 'increments each time this record was changed, for optimistic locking'
);

Alter table two_factor_service_provider
  add primary key (UUID);


CREATE UNIQUE INDEX TWO_FACTOR_SP_ID_IDX ON two_factor_service_provider
(SERVICE_PROVIDER_ID);










CREATE TABLE two_factor_user_agent
(
  UUID              VARCHAR(40 )           NOT NULL comment 'this is the primary key for the table, uuid',
  USER_AGENT        VARCHAR(200 )          NOT NULL comment 'user agent sent from the browser',
  BROWSER           VARCHAR(30 ) comment 'assumed browser from user agent',
  OPERATING_SYSTEM  VARCHAR(30 ) comment 'assumed OS from the user agent',
  MOBILE            VARCHAR(1 ) comment 'if this is a mobile device',
  DELETED_ON                    BIGINT(20)    comment 'If this row is deleted, set a delete date, and delete it later',
  LAST_UPDATED                  BIGINT(20)    NOT NULL comment 'millis since 1970 that this record has been updated',
  VERSION_NUMBER    BIGINT(20)                     NOT NULL comment 'increments each time the record is stored for optimistic locking'
);

CREATE UNIQUE INDEX TWO_FACTOR_AGENT_AGENT_IDX ON two_factor_user_agent
(USER_AGENT);

Alter table two_factor_user_agent
  add primary key (UUID);

  
  
  
  
  

CREATE TABLE two_factor_user_attr
(
  ATTRIBUTE_NAME           VARCHAR(30) NOT NULL comment 'name of the attribute for this user: opted_in, sequential_pass_given_to_user, sequential_pass_index, two_factor_secret, two_factor_secret_temp',
  USER_UUID                VARCHAR(40) NOT NULL comment 'foreign key to two_factor_user',
  LAST_UPDATED             BIGINT(20) NOT NULL comment 'when this row was last updated',
  VERSION_NUMBER           BIGINT(20) NOT NULL comment 'hibernate version number to make sure two updates dont happen at once',
  ATTRIBUTE_VALUE_STRING   VARCHAR(100) comment 'if this is a string or boolean attribute, this is the value',
  ATTRIBUTE_VALUE_INTEGER  BIGINT(20) comment 'if this is an integer attribute, this is the value',
  DELETED_ON               BIGINT(20) comment 'if this row needs to be deleted, set a delete date for a week, then actually delete it',
  ENCRYPTION_TIMESTAMP     BIGINT(20) comment 'timestamp used for encrypting the data if applicable',
  UUID                     VARCHAR(40) NOT NULL comment 'primary key'
);

Alter table two_factor_user_attr
  add primary key (UUID);


CREATE UNIQUE INDEX TWO_FACTOR_USER_ATTR_U01 ON two_factor_user_attr
(ATTRIBUTE_NAME, USER_UUID);


CREATE INDEX TWO_FACTOR_USER_ATTR_USERIDX ON two_factor_user_attr
(USER_UUID);



ALTER TABLE two_factor_user_attr ADD (
  CONSTRAINT TWO_FACTOR_USER_ATTR_R01 
  FOREIGN KEY (USER_UUID) 
  REFERENCES two_factor_user (UUID));



  
  
  
CREATE TABLE two_factor_audit
(
  UUID                   VARCHAR(40)      NOT NULL COMMENT 'entry for every audit even that happens in the system',
  USER_UUID              VARCHAR(40) COMMENT 'unique id of the row',
  BROWSER_UUID           VARCHAR(40) COMMENT 'foreign key to the browser', 
  THE_TIMESTAMP          BIGINT(20)                NOT NULL COMMENT 'timestamp of this record',
  ACTION                 VARCHAR(30)      NOT NULL COMMENT 'action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, INVALIDATE_PASSWORDS, GENERATE_PASSWORDS',
  DESCRIPTION            VARCHAR(1000) COMMENT 'if a description is needed, this is more info',
  IP_ADDRESS_UUID        VARCHAR(40) COMMENT 'foreign key to the ip address',
  SERVICE_PROVIDER_UUID  VARCHAR(40) COMMENT 'foreign key to the service provider',
  USER_AGENT_UUID        VARCHAR(40) COMMENT 'foreign key to the user agent',
  VERSION_NUMBER         BIGINT(20) COMMENT 'increments each time the record is stored, for optimistic locking',
  DELETED_ON             BIGINT(20) COMMENT 'when this row was deleted millis since 1970, will be actually deleted in a week',
  USER_UUID_USING_APP    VARCHAR(40) COMMENT 'the user uuid (foreign key to user table) of the user using the app'
);

CREATE INDEX TF_AUDIT_DELETE_IDX ON two_factor_audit
(DELETED_ON);


CREATE INDEX two_factor_audit_ACTION_IDX ON two_factor_audit
(ACTION);


CREATE INDEX two_factor_audit_AGENT_IDX ON two_factor_audit
(USER_AGENT_UUID);


CREATE INDEX two_factor_audit_BROWSER_IDX ON two_factor_audit
(BROWSER_UUID);


CREATE INDEX two_factor_audit_IP_ADDR_IDX ON two_factor_audit
(IP_ADDRESS_UUID);


CREATE UNIQUE INDEX two_factor_audit_PK ON two_factor_audit
(UUID);

CREATE INDEX TWO_FACTOR_AUDIT_USER_USE_IDX ON TWO_FACTOR_AUDIT
(USER_UUID_USING_APP);


CREATE INDEX two_factor_audit_SP_IDX ON two_factor_audit
(SERVICE_PROVIDER_UUID);


CREATE INDEX two_factor_audit_USER_IDX ON two_factor_audit
(USER_UUID);

Alter table two_factor_audit
  add primary key (UUID);

ALTER TABLE two_factor_audit ADD (
  CONSTRAINT two_factor_audit_R01 
  FOREIGN KEY (USER_UUID) 
  REFERENCES two_factor_user (UUID));

ALTER TABLE two_factor_audit ADD (
  CONSTRAINT two_factor_audit_R02 
  FOREIGN KEY (BROWSER_UUID) 
  REFERENCES two_factor_browser (UUID));

ALTER TABLE two_factor_audit ADD (
  CONSTRAINT two_factor_audit_R03 
  FOREIGN KEY (SERVICE_PROVIDER_UUID) 
  REFERENCES two_factor_service_provider (UUID));

ALTER TABLE two_factor_audit ADD (
  CONSTRAINT two_factor_audit_R04 
  FOREIGN KEY (USER_AGENT_UUID) 
  REFERENCES two_factor_user_agent (UUID));

ALTER TABLE two_factor_audit ADD (
  CONSTRAINT two_factor_audit_R05 
  FOREIGN KEY (IP_ADDRESS_UUID) 
  REFERENCES two_factor_ip_address (UUID));

  
CREATE or replace
    VIEW two_factor_user_v (loginid, uuid, OPTED_IN, SEQUENTIAL_PASS_INDEX,
       SEQUENTIAL_PASS_GIVEN_TO_USER, TWO_FACTOR_SECRET_ABBR, TWO_FACTOR_SECRET_TEMP_ABBR,
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
   DATE_INVITED_COLLEAGUES_DATE
 )
    AS
(   SELECT tfu.loginid, tfu.uuid,
          (SELECT TFUA.ATTRIBUTE_VALUE_STRING
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'opted_in'),
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
                       (SELECT 
                 CASE
                   WHEN TFUA.ATTRIBUTE_VALUE_String is null
                     THEN NULL
                     ELSE concat(SUBSTRing(tfua.ATTRIBUTE_VALUE_String, 1, 3), '...')
                   END                       
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'two_factor_secret')
             AS two_factor_secret_abbr,
          (SELECT CASE
                   WHEN TFUA.ATTRIBUTE_VALUE_String is null
                     THEN NULL
                     ELSE concat(SUBSTRing(tfua.ATTRIBUTE_VALUE_String, 1, 3), '...')
                   END     
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
                        DATE_ADD( '1970-01-01', interval 
                          (TFUA.ATTRIBUTE_VALUE_INTEGER / 1000) SECOND)
                  END
             FROM two_factor_user_attr tfua
            WHERE TFUA.USER_UUID = TFU.UUID
                  AND TFUA.ATTRIBUTE_NAME = 'date_invited_colleagues')
             AS date_invited_colleagues_date    
 FROM two_factor_user tfu);


  


CREATE OR REPLACE VIEW two_factor_audit_v
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
             ELSE DATE_ADD( '1970-01-01', interval 
                          (tfa.the_timestamp / 1000) SECOND)                          
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
          (SELECT tfb.TRUSTED_BROWSER
             FROM two_factor_browser tfb
            WHERE tfb.uuid = tfa.browser_uuid)
             AS trusted_browser,
          (SELECT tfia.IP_ADDRESS
             FROM two_factor_ip_address tfia
            WHERE tfia.uuid = tfa.IP_ADDRESS_UUID)
             AS ip_address,
          (SELECT tfua.OPERATING_SYSTEM
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent_operating_system,
          (SELECT tfua.BROWSER
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent_browser,
          (SELECT tfua.MOBILE
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent_mobile,
          (SELECT tfsp.SERVICE_PROVIDER_ID
             FROM two_factor_service_provider tfsp
            WHERE tfsp.uuid = tfa.service_provider_uuid)
             AS service_provider_id,
          (SELECT tfsp.SERVICE_PROVIDER_name
             FROM two_factor_service_provider tfsp
            WHERE tfsp.uuid = tfa.service_provider_uuid)
             AS service_provider_name,
          tfa.DESCRIPTION,
          (SELECT tfia.DOMAIN_NAME
             FROM two_factor_ip_address tfia
            WHERE tfia.uuid = tfa.IP_ADDRESS_UUID)
             AS domain_name,
          (SELECT CASE
                     WHEN tfb.when_trusted = 0
                     THEN
                        NULL
                     ELSE DATE_ADD( '1970-01-01', interval 
                          (tfb.when_trusted / 1000) SECOND)
                  END
             FROM two_factor_browser tfb
            WHERE tfb.uuid = tfa.browser_uuid)
             AS when_browser_trusted_date,
          (SELECT tfua.user_agent
             FROM two_factor_user_agent tfua
            WHERE tfua.uuid = tfa.user_agent_uuid)
             AS user_agent,
          tfa.UUID,
          tfa.the_timestamp,
          (SELECT tfb.when_trusted
             FROM two_factor_browser tfb
            WHERE tfb.uuid = tfa.browser_uuid)
             AS when_browser_trusted,
          tfa.USER_UUID,
          tfa.BROWSER_UUID,
          tfa.IP_ADDRESS_UUID,
          tfa.SERVICE_PROVIDER_UUID,
          tfa.USER_AGENT_UUID,
          tfa.USER_UUID_USING_APP
     FROM two_factor_audit tfa
    WHERE tfa.DELETED_ON IS NULL;

 

CREATE TABLE two_factor_sample_source
(
  subject_id                    VARCHAR(100 ) NOT NULL comment 'opaque id for user of system',
  net_id                        VARCHAR(100 ) NOT NULL comment 'net_id of the user that they use to login with',
  name                          VARCHAR(200 ) NOT NULL comment 'first and last name of user',
  description                   VARCHAR(200 ) NOT NULL comment 'what is shown on the screen for this user',
  search_string_lower           VARCHAR(200 ) NOT NULL comment 'lower case stuff to search on',
  email                         VARCHAR(256 ) NOT NULL comment 'email address of user',
  active                        VARCHAR(256 ) NOT NULL comment 'T or F for if the user is active'
);

CREATE UNIQUE INDEX two_factor_sample_source_idx ON two_factor_sample_source (subject_id);

alter table two_factor_sample_source add primary key (subject_id);

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

