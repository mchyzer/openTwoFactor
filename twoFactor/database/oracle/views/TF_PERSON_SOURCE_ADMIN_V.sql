CREATE OR REPLACE FORCE VIEW tf_person_source_admin_v
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
             ELSE 'F'
          END
             AS active
     /* (select 'T' from authzadm.PROJECT_TF_ACTIVE_USERS_V ptauv where ptauv.penn_id = cpv.penn_id) as active */
     FROM pcdadmin.computed_person_v cpv
    WHERE kerberos_principal IS NOT NULL;
COMMENT ON TABLE tf_person_source_admin_v IS 'person source to lookup and search for subjects in the two factor project';

COMMENT ON COLUMN tf_person_source_admin_v.PENN_ID IS 'numeric penn_id';

COMMENT ON COLUMN tf_person_source_admin_v.PENNNAME IS 'pennkey of the user';

COMMENT ON COLUMN tf_person_source_admin_v.DESCRIPTION IS 'description to show on screen';

COMMENT ON COLUMN tf_person_source_admin_v.SEARCH_DESCRIPTION IS 'lower search description to search with';

COMMENT ON COLUMN tf_person_source_admin_v.NAME IS 'name of user';

COMMENT ON COLUMN tf_person_source_admin_v.EMAIL IS 'email of user';

COMMENT ON COLUMN tf_person_source_admin_v.ACTIVE IS 'T if active, F if not';
