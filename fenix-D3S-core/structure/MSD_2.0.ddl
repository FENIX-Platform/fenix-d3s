CONNECT remote:localhost/msd_2.0 admin admin;


CREATE CLASS MeAccessibility;
CREATE CLASS MeComparability;
CREATE CLASS MeContent;
CREATE CLASS SeCodeList;
CREATE CLASS SeReferencePopulation;
CREATE CLASS SeCoverage;
CREATE CLASS MeDataQuality;
CREATE CLASS MeDocuments;
CREATE CLASS MeIdentification;
CREATE CLASS MeInstitutionalMandate;
CREATE CLASS MeMaintenance;
CREATE CLASS MeReferenceSystem;
CREATE CLASS MeResourceDimensions;
CREATE CLASS MeSpatialRepresentation;
CREATE CLASS MeStatisticalProcessing;

CREATE CLASS OjResponsibleParty;
CREATE CLASS OjContact;
CREATE CLASS OjCitation;
CREATE CLASS OjCodeList;
CREATE CLASS OjCode;
CREATE CLASS OjMeasure;

CREATE CLASS Period;
CREATE CLASS Code;



CREATE PROPERTY Period.from INTEGER;
CREATE PROPERTY Period.to INTEGER;

CREATE PROPERTY OjCodeList.linkedCodes LINKLIST Code;
CREATE PROPERTY OjCodeList.codeList STRING;
CREATE PROPERTY OjCodeList.version STRING;
CREATE PROPERTY OjCodeList.codes EMBEDDEDLIST OjCode;
CREATE PROPERTY OjCodeList.title EMBEDDEDMAP STRING;
CREATE PROPERTY OjCodeList.contactInfo EMBEDDED OjResponsibleParty;
CREATE PROPERTY OjCodeList.link STRING;

CREATE PROPERTY OjCode.code STRING;
CREATE PROPERTY OjCode.title EMBEDDEDMAP STRING;

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
CREATE PROPERTY OjCitation.documentContact EMBEDDED OjResponsibleParty;
CREATE PROPERTY OjCitation.notes EMBEDDEDMAP STRING;
CREATE PROPERTY OjCitation.link STRING;
CREATE PROPERTY OjCitation.periodicity EMBEDDED OjCodeList;
CREATE PROPERTY OjCitation.ISBN STRING;
CREATE PROPERTY OjCitation.ISSN STRING;


CREATE PROPERTY Code.codeList LINK MeIdentification;
CREATE PROPERTY Code.code STRING;
CREATE PROPERTY Code.level INTEGER;
CREATE PROPERTY Code.title EMBEDDEDMAP STRING;
CREATE PROPERTY Code.description EMBEDDEDMAP STRING;
CREATE PROPERTY Code.validityPeriod EMBEDDED Period;
CREATE PROPERTY Code.parents LINKLIST Code;
CREATE PROPERTY Code.children LINKLIST Code;
CREATE PROPERTY Code.relations LINKLIST Code;


CREATE PROPERTY MeIdentification.uid STRING;
CREATE PROPERTY MeIdentification.version STRING;
CREATE PROPERTY MeIdentification.parentsIdentifier EMBEDDEDSET STRING;
CREATE PROPERTY MeIdentification.languages EMBEDDEDLIST OjCodeList;
CREATE PROPERTY MeIdentification.languageDetail EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.title EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.creationDate DATE;
CREATE PROPERTY MeIdentification.characterSet EMBEDDED OjCodeList;
CREATE PROPERTY MeIdentification.metadataStandardName EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.metadataStandardVersion EMBEDDEDMAP STRING;
CREATE PROPERTY MeIdentification.metadataLanguage EMBEDDEDLIST OjCodeList;
CREATE PROPERTY MeIdentification.contacts EMBEDDEDLIST OjResponsibleParty;
CREATE PROPERTY MeIdentification.noDataValue STRING;
CREATE PROPERTY MeIdentification.meContent EMBEDDED MeContent;


CREATE PROPERTY MeContent.resourceRepresentationType STRING;
CREATE PROPERTY MeContent.keyWords EMBEDDEDLIST STRING;
CREATE PROPERTY MeContent.description EMBEDDEDMAP STRING;
CREATE PROPERTY MeContent.seReferencePopulation EMBEDDED SeReferencePopulation;
CREATE PROPERTY MeContent.seCoverage EMBEDDED SeCoverage;
CREATE PROPERTY MeContent.seCodeList EMBEDDED SeCodeList;

CREATE PROPERTY SeReferencePopulation.statisticalPopulation EMBEDDEDMAP STRING;
CREATE PROPERTY SeReferencePopulation.statisticalUnit EMBEDDEDMAP STRING;
CREATE PROPERTY SeReferencePopulation.referencePeriod EMBEDDED OjCodeList;
CREATE PROPERTY SeReferencePopulation.referenceArea EMBEDDED OjCodeList;

CREATE PROPERTY SeCoverage.coverageSector EMBEDDEDLIST OjCodeList;
CREATE PROPERTY SeCoverage.coverageSectorDetails EMBEDDEDMAP STRING;
CREATE PROPERTY SeCoverage.coverageTime EMBEDDED Period;
CREATE PROPERTY SeCoverage.coverageGeographic EMBEDDEDLIST OjCodeList;

CREATE PROPERTY SeCodeList.numberOfLevels INTEGER;
CREATE PROPERTY SeCodeList.typeOfCodeList STRING;



DISCONNECT;