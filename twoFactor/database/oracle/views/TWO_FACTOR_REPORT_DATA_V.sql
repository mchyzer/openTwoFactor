/* Formatted on 4/7/2014 5:28:42 PM (QP5 v5.163.1008.3004) */
CREATE OR REPLACE FORCE VIEW TWO_FACTOR_REPORT_DATA_V
(
   LOGINID,
   REPORT_NAME_SYSTEM
)
AS
   SELECT DISTINCT m.char_penn_id AS loginid, A.PENN_PAY_ORG AS report_name_system
     FROM COMADMIN.MEMBER m,
          comadmin.affiliation a,
          DIRADMIN.DIR_DETAIL_AFFILIATION_V t,
          diradmin.dir_detail_name_v n
    WHERE     t.penn_id = m.penn_id
          AND m.penn_id = a.penn_id
          AND m.penn_id = n.penn_id
          AND a.active_code = 'A'
          AND a.source = 'PENNPAY'
          AND N.PREF_FLAG_NAME = 'Y'
          AND N.VIEW_TYPE = 'I'
          AND T.PREF_FLAG_AFFIL = 'Y'
          AND T.VIEW_TYPE = 'I';
COMMENT ON TABLE TWO_FACTOR_REPORT_DATA_V IS 'report data for two step';

COMMENT ON COLUMN TWO_FACTOR_REPORT_DATA_V.LOGINID IS 'login id of user';

COMMENT ON COLUMN TWO_FACTOR_REPORT_DATA_V.REPORT_NAME_SYSTEM IS 'report name system of report the user is in';
