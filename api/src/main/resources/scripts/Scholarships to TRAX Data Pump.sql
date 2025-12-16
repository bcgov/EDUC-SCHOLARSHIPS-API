TRUNCATE TABLE STUDENT@sldlink.world;
TRUNCATE TABLE SCHOOL_FUNDING_MASTER@sldlink.world;

CREATE TABLE SCHOOL_FUNDING_GROUP_CODE (
    SCHOOL_FUNDING_GROUP_CODE VARCHAR(10) NOT NULL,
    LABEL VARCHAR(30) NOT NULL,
    DESCRIPTION VARCHAR(255) NOT NULL,
    LEGACY_CODE VARCHAR(10) NULL
);

--Create tables in the migration schema
CREATE TABLE SDC_SCHOOL_COLLECTION
(
    SCHOOL_ID VARCHAR2(40),
    COLLECTION_ID VARCHAR2(40),
    SDC_SCHOOL_COLLECTION_ID VARCHAR2(40)
);

--Create tables in the migration schema
CREATE TABLE INDEPENDENT_SCHOOL_FUNDING_GROUP_SNAPSHOT
(	    
     COLLECTION_ID VARCHAR2(40),
     SCHOOL_ID VARCHAR2(40),
     SCHOOL_GRADE_CODE VARCHAR2(10),
     SCHOOL_FUNDING_GROUP_CODE VARCHAR2(10)
);

--Copy tables into the migration schema with the following fields
SELECT
    sdc_student.ASSIGNED_PEN,
    sdc_student.ENROLLED_GRADE_CODE,
    sdc_student.SCHOOL_FUNDING_CODE,
    sdc_student.DOB,
    sdc_student.FTE,
    sdc_student.SDC_SCHOOL_COLLECTION_ID,
    sdc_student.SDC_SCHOOL_COLLECTION_STUDENT_STATUS_CODE
FROM SDC_SCHOOL_COLLECTION_STUDENT sdc_student, SDC_SCHOOL_COLLECTION school
WHERE sdc_student.SDC_SCHOOL_COLLECTION_ID = school.SDC_SCHOOL_COLLECTION_ID
  AND school.COLLECTION_ID = :collectionID;

SELECT
    school.collection_id,
    school.school_id,
    school.sdc_school_collection_id
FROM SDC_SCHOOL_COLLECTION school
WHERE school.COLLECTION_ID = :collectionID;

SELECT 
    collection_id, 
    school_id, 
    school_grade_code, 
    school_funding_group_code
FROM INDEPENDENT_SCHOOL_FUNDING_GROUP_SNAPSHOT funding
where collection_id =  :collectionID;

--Copy SCHOOL_FUNDING_GROUP_CODE table from SDC API into the migration schema
select
    school_funding_group_code,
    label,
    description,
    legacy_code
from school_funding_group_code;

--Copy both DISTRICT and SCHOOL tables from the target environment into the migration schema


--Run in migration schema
CREATE TABLE SLD_STUDENT_SCHOLARSHIPS_PUMP
AS
SELECT
    sdc_student.ASSIGNED_PEN as STUDENT_ID,
    sdc_student.ASSIGNED_PEN as STUD_NO,
    (SELECT dist.DISTRICT_NUMBER from DISTRICT dist, SCHOOL school WHERE school.SCHOOL_ID = sdc_school.SCHOOL_ID AND dist.DISTRICT_ID = school.DISTRICT_ID) as DISTNO,
    (SELECT school.SCHOOL_NUMBER from SCHOOL school WHERE school.SCHOOL_ID = sdc_school.SCHOOL_ID) as SCHLNO,
    sdc_student.ENROLLED_GRADE_CODE as ENROLLED_GRADE_CODE,
    sdc_student.SCHOOL_FUNDING_CODE as SCHOOL_FUNDING_CODE,
    sdc_student.DOB as BIRTH_DATE,
    sdc_student.FTE * 100 as STUDENT_FTE_VALUE,
    :enterReportDate as REPORT_DATE
FROM SDC_SCHOOL_COLLECTION_STUDENT sdc_student, SDC_SCHOOL_COLLECTION sdc_school
WHERE sdc_student.SDC_SCHOOL_COLLECTION_ID = sdc_school.SDC_SCHOOL_COLLECTION_ID
AND sdc_school.COLLECTION_ID = :collectionID
AND sdc_student.SDC_SCHOOL_COLLECTION_STUDENT_STATUS_CODE = 'COMPLETED';

-----------------------------------------

--Run in migration schema
CREATE TABLE SCHOOL_FUNDING_MASTER_PUMP
AS
WITH funding_counts AS (
    SELECT
        fund.SCHOOL_ID,
        fund.SCHOOL_FUNDING_GROUP_CODE,
        COUNT(*) AS code_count,
        ROW_NUMBER() OVER (
            PARTITION BY fund.SCHOOL_ID 
            ORDER BY COUNT(*) DESC, fund.SCHOOL_FUNDING_GROUP_CODE
        ) AS rn
    FROM INDEPENDENT_SCHOOL_FUNDING_GROUP_SNAPSHOT fund, school schl
    WHERE fund.collection_id = :collection_id
      AND fund.school_grade_code in ('GRADE11','GRADE12')
      AND schl.FACILITY_TYPE_CODE NOT IN ('DIST_LEARN')
      AND schl.school_id = fund.school_id
    GROUP BY fund.SCHOOL_ID, fund.SCHOOL_FUNDING_GROUP_CODE
)
SELECT
    (SELECT dist.DISTRICT_NUMBER from DISTRICT dist, SCHOOL school WHERE school.SCHOOL_ID = fund_counts.school_id AND dist.DISTRICT_ID = school.DISTRICT_ID) as DISTNO,
    (SELECT school.SCHOOL_NUMBER from SCHOOL school WHERE school.SCHOOL_ID = fund_counts.school_id) as SCHLNO,
    (SELECT fund_group.legacy_code from SCHOOL_FUNDING_GROUP_CODE fund_group WHERE fund_group.school_funding_group_code = fund_counts.school_funding_group_code) as FUNDING_GROUP_CODE,
    '11' AS FUNDING_GROUP_SUBCODE,
    '20250101' as CREATE_DATE,
    '11134847' as CREATE_TIME,
    'SCHOLARS' as CREATE_USERNAME,
    '20250101' as EDIT_DATE,
    'SCHOLARS' as EDIT_TIME,
    'SCHOLARS' as EDIT_USERNAME
FROM FUNDING_COUNTS fund_counts
WHERE rn = 1
ORDER BY school_id;


----------------------------------------

DECLARE
v_batch_size NUMBER := 1000;
  v_counter NUMBER := 0;
BEGIN
FOR rec IN (
    SELECT
      STUDENT_ID, DISTNO, SCHLNO, REPORT_DATE, ENROLLED_GRADE_CODE,
      STUDENT_FTE_VALUE, BIRTH_DATE, PEN, SCHOOL_FUNDING_CODE
    FROM SLD_STUDENT_SCHOLARSHIPS_PUMP
  ) LOOP
    INSERT INTO STUDENT@SLDLINK.WORLD VALUES (
      rec.STUDENT_ID, rec.DISTNO, rec.SCHLNO, rec.REPORT_DATE,
      NULL, NULL, rec.ENROLLED_GRADE_CODE, rec.STUDENT_FTE_VALUE,
      NULL, NULL, NULL, NULL, NULL, rec.BIRTH_DATE, NULL, NULL,
      NULL, NULL, NULL, NULL, NULL, NULL, NULL, rec.PEN, NULL,
      rec.SCHOOL_FUNDING_CODE, NULL, NULL, NULL, NULL, NULL,
      NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
    );
    
    v_counter := v_counter + 1;
    
    IF MOD(v_counter, v_batch_size) = 0 THEN
      COMMIT;
      DBMS_OUTPUT.PUT_LINE('Committed ' || v_counter || ' rows');
END IF;
END LOOP;

COMMIT; -- Final commit for remaining rows
DBMS_OUTPUT.PUT_LINE('Total rows inserted: ' || v_counter);
END;


---------------------------------------------
SELECT
    'UPDATE SCHOOL_FUNDING_MASTER@spmlink.world SET FUNDING_GROUP_CODE=''' || sfm.FUNDING_GROUP_CODE ||
    ''' WHERE DISTNO=''' || sfm.DISTNO ||
    ''' AND FUNDING_GROUP_SUBCODE=''' || sfm.FUNDING_GROUP_SUBCODE ||
    ''' AND SCHLNO=''' || sfm.SCHLNO || ''';'
FROM SCHOOL_FUNDING_MASTER_PUMP sfm;

--Take update statements and run them

SELECT
    'INSERT INTO SCHOOL_FUNDING_MASTER@spmlink.world (DISTNO, SCHLNO, FUNDING_GROUP_CODE, FUNDING_GROUP_SUBCODE) VALUES (
    ' || sssp.DISTNO || ', 
    ' || sssp.SCHLNO || ',
	' || sssp.FUNDING_GROUP_CODE || ',
	' || sssp.FUNDING_GROUP_SUBCODE || ');'
FROM SCHOOL_FUNDING_MASTER_PUMP sssp WHERE sssp.DISTNO || sssp.SCHLNO
                                               NOT IN (SELECT fm.DISTNO || fm.SCHLNO FROM SCHOOL_FUNDING_MASTER@spmlink.world fm WHERE fm.FUNDING_GROUP_SUBCODE = '11');

--Take insert statements, remove the double quotes and run them
