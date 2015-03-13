/* Formatted on 3/13/2015 2:01:52 AM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TF_PERSON_SOURCE_ADMIN_V
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
          directory_pref_cent_disp_name AS name,
          ADMIN_VIEW_PREF_EMAIL_ADDRESS AS email,
          CASE
             WHEN CPV.IS_ACTIVE_FACULTY = 'Y' THEN 'T'
             WHEN CPV.IS_ACTIVE_STAFF = 'Y' THEN 'T'
             WHEN CPV.IS_ACTIVE_STUDENT = 'Y' THEN 'T'
             WHEN TSMA.ACTIVE = 'T' THEN 'T'
             ELSE 'F'
          END
             AS active
     /* (select 'T' from authzadm.PROJECT_TF_ACTIVE_USERS_V ptauv where ptauv.penn_id = cpv.penn_id) as active */
     FROM pcdadmin.computed_person_v cpv, tf_source_make_active tsma
    WHERE kerberos_principal IS NOT NULL
      AND TSMA.PENN_ID (+) = CPV.CHAR_PENN_ID ;
    

COMMENT ON TABLE TF_PERSON_SOURCE_ADMIN_V IS 'person source to lookup and search for subjects in the two factor project for admin users';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.PENN_ID IS 'numeric penn_id';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.PENNNAME IS 'pennkey of the user';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.DESCRIPTION IS 'description to show on screen';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.SEARCH_DESCRIPTION IS 'lower search description to search with';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.NAME IS 'name of user';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.EMAIL IS 'email of user';

COMMENT ON COLUMN TF_PERSON_SOURCE_ADMIN_V.ACTIVE IS 'T if active, F if not';



GRANT SELECT ON TF_PERSON_SOURCE_ADMIN_V TO TWO_FACTOR_ADMIN;
