/* Formatted on 8/19/2019 8:52:44 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW TWO_STEP_REQUIRED_STUDENTS_V
(
   SUBJECT_ID,
   SUBJECT_SOURCE_ID
)
   BEQUEATH DEFINER
AS
   SELECT penn_id AS subject_id, 'pennperson' AS subject_source_id
     FROM authzadm.TWO_STEP_REPORT_HELPER_V tsrrv
    WHERE     TSRRV.CENTER_CODE NOT IN ('VM', '58')
          AND active_code = 'A'
          AND IS_STUDENT = 'T';
