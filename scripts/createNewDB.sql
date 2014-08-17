
SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

----------------------------------------------
--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- ЗАМЕНИТЬ ВСЕ (!) finARM2 НА ИМЯ НОВОЙ БАЗЫ!
--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

CREATE DATABASE "finARM2" WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'Russian_Russia.1251' LC_CTYPE = 'Russian_Russia.1251';


ALTER DATABASE "finARM2" OWNER TO postgres;

CREATE ROLE "finARM2_users" NOSUPERUSER INHERIT NOCREATEDB CREATEROLE NOREPLICATION;
GRANT CONNECT, TEMPORARY ON DATABASE "finARM2" TO GROUP "armDirectors";
GRANT CONNECT, TEMPORARY ON DATABASE "finARM2" TO GROUP "finARM2_users";
REVOKE ALL ON DATABASE "finARM2" FROM public;

\connect "finARM2"

--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- НИЖЕ НИЧЕГО НЕ МЕНЯТЬ!
--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
----------------------------------------------

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 6 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO postgres;

--
-- TOC entry 2017 (class 0 OID 0)
-- Dependencies: 6
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- TOC entry 182 (class 3079 OID 11750)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2019 (class 0 OID 0)
-- Dependencies: 182
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- TOC entry 189 (class 1255 OID 66417)
-- Name: OnFinOpInsert(); Type: FUNCTION; Schema: public; Owner: director
--

CREATE FUNCTION "OnFinOpInsert"() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
   NEW.operationdate=current_date;
   RETURN NEW;
END;$$;


ALTER FUNCTION public."OnFinOpInsert"() OWNER TO director;

--
-- TOC entry 196 (class 1255 OID 66418)
-- Name: OnIncInsert(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION "OnIncInsert"() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
   NEW.incomingdate = current_date;
   RETURN NEW;
END;$$;


ALTER FUNCTION public."OnIncInsert"() OWNER TO postgres;

--
-- TOC entry 197 (class 1255 OID 66419)
-- Name: OnSpendInsert(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION "OnSpendInsert"() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
   NEW.paymentdate = current_date;
   RETURN NEW;
END;$$;


ALTER FUNCTION public."OnSpendInsert"() OWNER TO postgres;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 170 (class 1259 OID 66420)
-- Name: financialmonth; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE financialmonth (
    fmid integer NOT NULL,
    month integer,
    year integer,
    closed boolean
);


ALTER TABLE public.financialmonth OWNER TO director;

--
-- TOC entry 171 (class 1259 OID 66423)
-- Name: financialoperation; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE financialoperation (
    foid integer NOT NULL,
    kind integer,
    operationdate date,
    operationsum double precision,
    managerid integer,
    customer character varying(255),
    ordernum character varying(255),
    paymenttype integer,
    closed boolean,
    closedate date,
    closeyear integer,
    closemonth integer,
    monthspid integer,
    nonplannedspending character varying(255),
    currentprofit double precision,
    salarysum double precision,
    currentsalaryprofit double precision,
    managerpercent double precision,
    closedforsalary boolean,
    closeforsalarydate date,
    closeforsalaryyear integer,
    closeforsalarymonth integer
);


ALTER TABLE public.financialoperation OWNER TO director;

--
-- TOC entry 172 (class 1259 OID 66429)
-- Name: financialoperationchanges; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE financialoperationchanges (
    changeid integer NOT NULL,
    foid integer,
    changedate date,
    whatchanged text,
    whomchanged character varying(255)
);


ALTER TABLE public.financialoperationchanges OWNER TO director;

--
-- TOC entry 173 (class 1259 OID 66435)
-- Name: fo_incomings; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE fo_incomings (
    foid integer NOT NULL,
    incomingid integer NOT NULL
);


ALTER TABLE public.fo_incomings OWNER TO director;

--
-- TOC entry 174 (class 1259 OID 66438)
-- Name: fo_spendings; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE fo_spendings (
    foid integer NOT NULL,
    finspid integer NOT NULL
);


ALTER TABLE public.fo_spendings OWNER TO director;

--
-- TOC entry 175 (class 1259 OID 66441)
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: director
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO director;

--
-- TOC entry 176 (class 1259 OID 66443)
-- Name: incoming; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE incoming (
    incomingid integer NOT NULL,
    incomingdate date,
    incomingsum double precision,
    incomingcomment character varying(255)
);


ALTER TABLE public.incoming OWNER TO director;

--
-- TOC entry 177 (class 1259 OID 66446)
-- Name: manager; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE manager (
    managerid integer NOT NULL,
    fio character varying(255),
    incomedate date,
    subsidy double precision,
    retention double precision,
    cashpercent double precision,
    noncashpercent double precision,
    dismissed boolean,
    dismissdate date,
    salary double precision
);


ALTER TABLE public.manager OWNER TO director;

--
-- TOC entry 178 (class 1259 OID 66449)
-- Name: managerpermonth; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE managerpermonth (
    mngpermonthid integer NOT NULL,
    managerid integer,
    fio character varying(255),
    incomedate date,
    subsidy double precision,
    retention double precision,
    cashpercent double precision,
    noncashpercent double precision,
    dismissed boolean,
    dismissdate date,
    salary double precision,
    month integer,
    year integer
);


ALTER TABLE public.managerpermonth OWNER TO director;

--
-- TOC entry 179 (class 1259 OID 66452)
-- Name: monthspending; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE monthspending (
    monthspid integer NOT NULL,
    month integer,
    year integer,
    name character varying(255),
    amount double precision
);


ALTER TABLE public.monthspending OWNER TO director;

--
-- TOC entry 180 (class 1259 OID 66455)
-- Name: spending; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE spending (
    finspid integer NOT NULL,
    payerto character varying(255),
    ordernum character varying(255),
    paymentsum double precision,
    paymenttype integer,
    paymentdate date,
    comment character varying(255),
    paymentsalarysum double precision
);


ALTER TABLE public.spending OWNER TO director;

--
-- TOC entry 181 (class 1259 OID 66461)
-- Name: spendingtemplate; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE spendingtemplate (
    stid integer NOT NULL,
    spendname character varying(255),
    spendamount double precision
);


ALTER TABLE public.spendingtemplate OWNER TO director;

--
-- TOC entry 1871 (class 2606 OID 66465)
-- Name: financialmonth_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY financialmonth
    ADD CONSTRAINT financialmonth_pkey PRIMARY KEY (fmid);


--
-- TOC entry 1873 (class 2606 OID 66467)
-- Name: financialoperation_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY financialoperation
    ADD CONSTRAINT financialoperation_pkey PRIMARY KEY (foid);


--
-- TOC entry 1875 (class 2606 OID 66469)
-- Name: financialoperationchanges_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY financialoperationchanges
    ADD CONSTRAINT financialoperationchanges_pkey PRIMARY KEY (changeid);


--
-- TOC entry 1877 (class 2606 OID 66471)
-- Name: fo_incomings_incomingid_key; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY fo_incomings
    ADD CONSTRAINT fo_incomings_incomingid_key UNIQUE (incomingid);


--
-- TOC entry 1879 (class 2606 OID 66473)
-- Name: fo_incomings_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY fo_incomings
    ADD CONSTRAINT fo_incomings_pkey PRIMARY KEY (foid, incomingid);


--
-- TOC entry 1881 (class 2606 OID 66475)
-- Name: fo_spendings_finspid_key; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fo_spendings_finspid_key UNIQUE (finspid);


--
-- TOC entry 1883 (class 2606 OID 66477)
-- Name: fo_spendings_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fo_spendings_pkey PRIMARY KEY (foid, finspid);


--
-- TOC entry 1885 (class 2606 OID 66479)
-- Name: incoming_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY incoming
    ADD CONSTRAINT incoming_pkey PRIMARY KEY (incomingid);


--
-- TOC entry 1887 (class 2606 OID 66481)
-- Name: manager_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY manager
    ADD CONSTRAINT manager_pkey PRIMARY KEY (managerid);


--
-- TOC entry 1889 (class 2606 OID 66483)
-- Name: managerpermonth_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY managerpermonth
    ADD CONSTRAINT managerpermonth_pkey PRIMARY KEY (mngpermonthid);


--
-- TOC entry 1891 (class 2606 OID 66485)
-- Name: monthspending_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY monthspending
    ADD CONSTRAINT monthspending_pkey PRIMARY KEY (monthspid);


--
-- TOC entry 1893 (class 2606 OID 66487)
-- Name: spending_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY spending
    ADD CONSTRAINT spending_pkey PRIMARY KEY (finspid);


--
-- TOC entry 1895 (class 2606 OID 66489)
-- Name: spendingtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY spendingtemplate
    ADD CONSTRAINT spendingtemplate_pkey PRIMARY KEY (stid);


--
-- TOC entry 1902 (class 2620 OID 66490)
-- Name: OnInsert; Type: TRIGGER; Schema: public; Owner: director
--

CREATE TRIGGER "OnInsert" BEFORE INSERT ON financialoperation FOR EACH ROW EXECUTE PROCEDURE "OnFinOpInsert"();


--
-- TOC entry 1903 (class 2620 OID 66491)
-- Name: OnInsert; Type: TRIGGER; Schema: public; Owner: director
--

CREATE TRIGGER "OnInsert" BEFORE INSERT ON incoming FOR EACH ROW EXECUTE PROCEDURE "OnIncInsert"();


--
-- TOC entry 1904 (class 2620 OID 66492)
-- Name: OnInsert; Type: TRIGGER; Schema: public; Owner: director
--

CREATE TRIGGER "OnInsert" BEFORE INSERT ON spending FOR EACH ROW EXECUTE PROCEDURE "OnSpendInsert"();


--
-- TOC entry 1896 (class 2606 OID 66493)
-- Name: fk9465defe2439d7a6; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY financialoperation
    ADD CONSTRAINT fk9465defe2439d7a6 FOREIGN KEY (monthspid) REFERENCES monthspending(monthspid);


--
-- TOC entry 1897 (class 2606 OID 66498)
-- Name: fk9465defe8dfee93f; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY financialoperation
    ADD CONSTRAINT fk9465defe8dfee93f FOREIGN KEY (managerid) REFERENCES manager(managerid);


--
-- TOC entry 1898 (class 2606 OID 66503)
-- Name: fkf2a96d174c503038; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY fo_incomings
    ADD CONSTRAINT fkf2a96d174c503038 FOREIGN KEY (foid) REFERENCES financialoperation(foid);


--
-- TOC entry 1899 (class 2606 OID 66508)
-- Name: fkf2a96d175ebf191d; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY fo_incomings
    ADD CONSTRAINT fkf2a96d175ebf191d FOREIGN KEY (incomingid) REFERENCES incoming(incomingid);


--
-- TOC entry 1900 (class 2606 OID 66513)
-- Name: fkf4b0e7394c503038; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fkf4b0e7394c503038 FOREIGN KEY (foid) REFERENCES financialoperation(foid);


--
-- TOC entry 1901 (class 2606 OID 66518)
-- Name: fkf4b0e739e470c5bd; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fkf4b0e739e470c5bd FOREIGN KEY (finspid) REFERENCES spending(finspid);


--
-- TOC entry 2018 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- TOC entry 2020 (class 0 OID 0)
-- Dependencies: 170
-- Name: financialmonth; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE financialmonth FROM PUBLIC;
REVOKE ALL ON TABLE financialmonth FROM director;
GRANT ALL ON TABLE financialmonth TO director;
GRANT ALL ON TABLE financialmonth TO "armDirectors";
GRANT ALL ON TABLE financialmonth TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE financialmonth TO "armViewers";


--
-- TOC entry 2021 (class 0 OID 0)
-- Dependencies: 171
-- Name: financialoperation; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE financialoperation FROM PUBLIC;
REVOKE ALL ON TABLE financialoperation FROM director;
GRANT ALL ON TABLE financialoperation TO director;
GRANT ALL ON TABLE financialoperation TO "armDirectors";
GRANT ALL ON TABLE financialoperation TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE financialoperation TO "armViewers";


--
-- TOC entry 2022 (class 0 OID 0)
-- Dependencies: 172
-- Name: financialoperationchanges; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE financialoperationchanges FROM PUBLIC;
REVOKE ALL ON TABLE financialoperationchanges FROM director;
GRANT ALL ON TABLE financialoperationchanges TO director;
GRANT ALL ON TABLE financialoperationchanges TO "armDirectors";
GRANT ALL ON TABLE financialoperationchanges TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE financialoperationchanges TO "armViewers";


--
-- TOC entry 2023 (class 0 OID 0)
-- Dependencies: 173
-- Name: fo_incomings; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE fo_incomings FROM PUBLIC;
REVOKE ALL ON TABLE fo_incomings FROM director;
GRANT ALL ON TABLE fo_incomings TO director;
GRANT ALL ON TABLE fo_incomings TO "armDirectors";
GRANT ALL ON TABLE fo_incomings TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE fo_incomings TO "armViewers";


--
-- TOC entry 2024 (class 0 OID 0)
-- Dependencies: 174
-- Name: fo_spendings; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE fo_spendings FROM PUBLIC;
REVOKE ALL ON TABLE fo_spendings FROM director;
GRANT ALL ON TABLE fo_spendings TO director;
GRANT ALL ON TABLE fo_spendings TO "armDirectors";
GRANT ALL ON TABLE fo_spendings TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE fo_spendings TO "armViewers";


--
-- TOC entry 2025 (class 0 OID 0)
-- Dependencies: 175
-- Name: hibernate_sequence; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON SEQUENCE hibernate_sequence FROM PUBLIC;
REVOKE ALL ON SEQUENCE hibernate_sequence FROM director;
GRANT ALL ON SEQUENCE hibernate_sequence TO director;
GRANT ALL ON SEQUENCE hibernate_sequence TO "armDirectors";
GRANT ALL ON SEQUENCE hibernate_sequence TO "armUsers";


--
-- TOC entry 2026 (class 0 OID 0)
-- Dependencies: 176
-- Name: incoming; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE incoming FROM PUBLIC;
REVOKE ALL ON TABLE incoming FROM director;
GRANT ALL ON TABLE incoming TO director;
GRANT ALL ON TABLE incoming TO "armDirectors";
GRANT ALL ON TABLE incoming TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE incoming TO "armViewers";


--
-- TOC entry 2027 (class 0 OID 0)
-- Dependencies: 177
-- Name: manager; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE manager FROM PUBLIC;
REVOKE ALL ON TABLE manager FROM director;
GRANT ALL ON TABLE manager TO director;
GRANT ALL ON TABLE manager TO "armDirectors";
GRANT ALL ON TABLE manager TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE manager TO "armViewers";


--
-- TOC entry 2028 (class 0 OID 0)
-- Dependencies: 178
-- Name: managerpermonth; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE managerpermonth FROM PUBLIC;
REVOKE ALL ON TABLE managerpermonth FROM director;
GRANT ALL ON TABLE managerpermonth TO director;
GRANT ALL ON TABLE managerpermonth TO "armDirectors";
GRANT ALL ON TABLE managerpermonth TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE managerpermonth TO "armViewers";


--
-- TOC entry 2029 (class 0 OID 0)
-- Dependencies: 179
-- Name: monthspending; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE monthspending FROM PUBLIC;
REVOKE ALL ON TABLE monthspending FROM director;
GRANT ALL ON TABLE monthspending TO director;
GRANT ALL ON TABLE monthspending TO "armDirectors";
GRANT ALL ON TABLE monthspending TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE monthspending TO "armViewers";


--
-- TOC entry 2030 (class 0 OID 0)
-- Dependencies: 180
-- Name: spending; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE spending FROM PUBLIC;
REVOKE ALL ON TABLE spending FROM director;
GRANT ALL ON TABLE spending TO director;
GRANT ALL ON TABLE spending TO "armDirectors";
GRANT ALL ON TABLE spending TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE spending TO "armViewers";


--
-- TOC entry 2031 (class 0 OID 0)
-- Dependencies: 181
-- Name: spendingtemplate; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE spendingtemplate FROM PUBLIC;
REVOKE ALL ON TABLE spendingtemplate FROM director;
GRANT ALL ON TABLE spendingtemplate TO director;
GRANT ALL ON TABLE spendingtemplate TO "armDirectors";
GRANT ALL ON TABLE spendingtemplate TO "armUsers";
GRANT SELECT,REFERENCES,TRIGGER ON TABLE spendingtemplate TO "armViewers";


--
-- TOC entry 1538 (class 826 OID 66524)
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: -; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres REVOKE ALL ON TABLES  FROM PUBLIC;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres REVOKE ALL ON TABLES  FROM postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TABLES  TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TABLES  TO "armDirectors";
ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TABLES  TO "armUsers";
ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT SELECT,REFERENCES,TRIGGER ON TABLES  TO "armViewers";


-- Completed on 2014-08-17 20:11:37

--
-- PostgreSQL database dump complete
--

