CONNECT remote:localhost/msd_1.0 admin admin;


CREATE CLASS MeAccessibility;
CREATE CLASS MeComparability;
CREATE CLASS MeContent;
CREATE CLASS MeDataQuality;
CREATE CLASS MeDocuments;
CREATE CLASS MeIdentification;
CREATE CLASS MeInstitutionalMandate;
CREATE CLASS MeMaintenance;
CREATE CLASS MeReferenceSystem;
CREATE CLASS MeResourceDimensions;
CREATE CLASS MeSpatialRepresentation;
CREATE CLASS MeStatisticalProcessing;
CREATE CLASS MeCodeList;

CREATE CLASS OjResponsibleParty;
CREATE CLASS OjContact;
CREATE CLASS OjCitation;
CREATE CLASS OjCode;
CREATE CLASS OjMeasure;

CREATE CLASS Period;



CREATE PROPERTY Period.from INTEGER;
CREATE PROPERTY Period.to INTEGER;

CREATE PROPERTY OjCode.codeList LINK MeIdentification;
CREATE PROPERTY OjCode.code STRING;
CREATE PROPERTY OjCode.title EMBEDDEDMAP STRING;
CREATE PROPERTY OjCode.description EMBEDDEDMAP STRING;
CREATE PROPERTY OjCode.validityPeriod EMBEDDED Period;
CREATE PROPERTY OjCode.parents LINKLIST OjCode;
CREATE PROPERTY OjCode.children LINKLIST OjCode;
CREATE PROPERTY OjCode.relations LINKLIST OjCode;

CREATE PROPERTY OjMeasure.extent EMBEDDEDMAP STRING;
CREATE PROPERTY OjMeasure.composed BOOLEAN;
CREATE PROPERTY OjMeasure.measurementSystem EMBEDDEDMAP STRING;
CREATE PROPERTY OjMeasure.name STRING;
CREATE PROPERTY OjMeasure.conversionToStandard DOUBLE;


CREATE PROPERTY OjResponsibleParty.organisation EMBEDDEDMAP STRING;
CREATE PROPERTY OjResponsibleParty.organisationUnit EMBEDDEDMAP STRING;
CREATE PROPERTY OjResponsibleParty.name STRING;
CREATE PROPERTY OjResponsibleParty.position EMBEDDEDMAP STRING;
CREATE PROPERTY OjResponsibleParty.role STRING;
CREATE PROPERTY OjResponsibleParty.specify EMBEDDEDMAP STRING;
CREATE PROPERTY OjResponsibleParty.contactInfo EMBEDDED OjContact;

CREATE PROPERTY OjContact.phone STRING;
CREATE PROPERTY OjContact.address STRING;
CREATE PROPERTY OjContact.emailAddress STRING;
CREATE PROPERTY OjContact.hoursOfService STRING;
CREATE PROPERTY OjContact.contactInstruction STRING;

CREATE PROPERTY OjCitation.documentKind STRING;
CREATE PROPERTY OjCitation.title EMBEDDEDMAP STRING;
CREATE PROPERTY OjCitation.date DATE;
CREATE PROPERTY OjCitation.documentContact OjResponsibleParty;
CREATE PROPERTY OjCitation.notes EMBEDDEDMAP STRING;
CREATE PROPERTY OjCitation.link STRING;
CREATE PROPERTY OjCitation.periodicity OjCode;
CREATE PROPERTY OjCitation.ISBN STRING;
CREATE PROPERTY OjCitation.ISSN STRING;





CREATE PROPERTY MeCodeList.uid STRING;
CREATE PROPERTY MeCodeList.version STRING;
CREATE PROPERTY MeCodeList.name EMBEDDEDMAP STRING;
CREATE PROPERTY MeCodeList.extendedName EMBEDDEDMAP STRING;
CREATE PROPERTY MeCodeList.contactInfo LINK OjResponsibleParty;
CREATE PROPERTY MeCodeList.link STRING;
CREATE PROPERTY MeCodeList.levelsNumber INTEGER;
CREATE PROPERTY MeCodeList.codes LINKLIST OjCode;
ALTER PROPERTY MeCodeList.uid MANDATORY TRUE;
ALTER PROPERTY MeCodeList.version MANDATORY TRUE;

CREATE PROPERTY MeIdentification.uid STRING;
CREATE PROPERTY MeIdentification.language LINKLIST OjCode;
CREATE PROPERTY MeIdentification.languageDetail EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.title EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.creationDate DATE;
CREATE PROPERTY MeIdentification.characterSet LINK OjCode;
CREATE PROPERTY MeIdentification.parentsIdentifier EMBEDDEDSET STRING;
CREATE PROPERTY MeIdentification.metadataStandardName EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.metadataStandardVersion EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.metadataLanguage LINKLIST OjCode;
CREATE PROPERTY MeIdentification.contacts LINKLIST OjResponsibleParty;
CREATE PROPERTY MeIdentification.noDataValue STRING;


CREATE PROPERTY MeContent.resourceRepresentationType STRING;
CREATE PROPERTY MeContent.keyWords EMBEDDEDLIST STRING;
CREATE PROPERTY MeContent.description STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;
CREATE PROPERTY MeContent. STRING;






















DISCONNECT;