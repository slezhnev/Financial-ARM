DROP TABLE financialoperationchanges;
CREATE TABLE financialoperationchanges
(
  changeid integer NOT NULL,
  foid integer,
  changedate date,
  whatchanged text,
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
