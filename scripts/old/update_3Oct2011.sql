CREATE TABLE financialoperationchanges
(
  changeid integer NOT NULL,
  foid integer,
  changedate date,
  whatchanged character varying(255),
  whomchanged character varying(255),
  CONSTRAINT financialoperationchanges_pkey PRIMARY KEY (changeid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE financialoperationchanges OWNER TO director;
GRANT ALL ON TABLE financialoperationchanges TO director;
GRANT ALL ON TABLE financialoperationchanges TO "armDirectors";
GRANT ALL ON TABLE financialoperationchanges TO "armUsers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE financialoperationchanges TO "armViewers";

CREATE OR REPLACE FUNCTION "OnFinOpInsert"()
  RETURNS trigger AS
$BODY$BEGIN
   NEW.operationdate=current_date;
   RETURN NEW;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION "OnFinOpInsert"() OWNER TO director;

CREATE OR REPLACE FUNCTION "OnIncInsert"()
  RETURNS trigger AS
$BODY$BEGIN
   NEW.incomingdate = current_date;
   RETURN NEW;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION "OnIncInsert"() OWNER TO postgres;

CREATE OR REPLACE FUNCTION "OnSpendInsert"()
  RETURNS trigger AS
$BODY$BEGIN
   NEW.paymentdate = current_date;
   RETURN NEW;
END;$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION "OnSpendInsert"() OWNER TO postgres;

CREATE TRIGGER "OnInsert"
  BEFORE INSERT
  ON financialoperation
  FOR EACH ROW
  EXECUTE PROCEDURE "OnFinOpInsert"();

CREATE TRIGGER "OnInsert"
  BEFORE INSERT
  ON incoming
  FOR EACH ROW
  EXECUTE PROCEDURE "OnIncInsert"();

CREATE TRIGGER "OnInsert"
  BEFORE INSERT
  ON spending
  FOR EACH ROW
  EXECUTE PROCEDURE "OnSpendInsert"();
