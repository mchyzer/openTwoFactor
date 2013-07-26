/* Formatted on 7/26/2013 1:03:36 AM (QP5 v5.163.1008.3004) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_V
(
   PENN_ID,
   PENNNAME,
   DESCRIPTION,
   SEARCH_DESCRIPTION,
   NAME,
   EMAIL,
   ACTIVE
)
AS
   SELECT char_penn_id AS penn_id,
          kerberos_principal AS pennname,
          description,
          search_description,
          NVL (PENNKEY_VIEW_PREF_NAME, kerberos_principal) AS name,
          ADMIN_VIEW_PREF_EMAIL_ADDRESS AS email,
          CASE
             WHEN CPV.IS_ACTIVE_FACULTY = 'Y' THEN 'T'
             WHEN CPV.IS_ACTIVE_STAFF = 'Y' THEN 'T'
             WHEN CPV.IS_ACTIVE_STUDENT = 'Y' THEN 'T'
             ELSE 'F'
          END
             AS active
     /* (select 'T' from authzadm.PROJECT_TF_ACTIVE_USERS_V ptauv where ptauv.penn_id = cpv.penn_id) as active */
     FROM pcdadmin.computed_person_v cpv
    WHERE kerberos_principal IS NOT NULL;
COMMENT ON TABLE TF_PERSON_SOURCE_V IS 'person source to lookup and search for subjects in the two factor project for non admin users';

COMMENT ON COLUMN TF_PERSON_SOURCE_V.PENN_ID IS 'numeric penn_id';

COMMENT ON COLUMN TF_PERSON_SOURCE_V.PENNNAME IS 'pennkey of the user';

COMMENT ON COLUMN TF_PERSON_SOURCE_V.DESCRIPTION IS 'description to show on screen';

COMMENT ON COLUMN TF_PERSON_SOURCE_V.SEARCH_DESCRIPTION IS 'lower search description to search with';

COMMENT ON COLUMN TF_PERSON_SOURCE_V.NAME IS 'name of user';

COMMENT ON COLUMN TF_PERSON_SOURCE_V.EMAIL IS 'email of user';

COMMENT ON COLUMN TF_PERSON_SOURCE_V.ACTIVE IS 'T if active, F if not';
