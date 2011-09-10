--
-- PostgreSQL database dump
--

-- Dumped from database version 9.0.2
-- Dumped by pg_dump version 9.0.2
-- Started on 2011-03-13 18:55:06

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 322 (class 2612 OID 11574)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;


ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1519 (class 1259 OID 16446)
-- Dependencies: 5
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
-- TOC entry 1522 (class 1259 OID 16559)
-- Dependencies: 5
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
    currentprofit double precision
);


ALTER TABLE public.financialoperation OWNER TO director;

--
-- TOC entry 1521 (class 1259 OID 16509)
-- Dependencies: 5
-- Name: fo_spendings; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE fo_spendings (
    foid integer NOT NULL,
    finspid integer NOT NULL
);


ALTER TABLE public.fo_spendings OWNER TO director;

--
-- TOC entry 1515 (class 1259 OID 16418)
-- Dependencies: 5
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
-- TOC entry 1516 (class 1259 OID 16426)
-- Dependencies: 5
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
-- TOC entry 1518 (class 1259 OID 16441)
-- Dependencies: 5
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
-- TOC entry 1520 (class 1259 OID 16464)
-- Dependencies: 5
-- Name: spending; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE spending (
    finspid integer NOT NULL,
    payerto character varying(255),
    ordernum character varying(255),
    paymentsum double precision,
    paymenttype integer,
    paymentdate date,
    comment character varying(255)
);


ALTER TABLE public.spending OWNER TO director;

--
-- TOC entry 1517 (class 1259 OID 16436)
-- Dependencies: 5
-- Name: spendingtemplate; Type: TABLE; Schema: public; Owner: director; Tablespace: 
--

CREATE TABLE spendingtemplate (
    stid integer NOT NULL,
    spendname character varying(255),
    spendamount double precision
);


ALTER TABLE public.spendingtemplate OWNER TO director;

--
-- TOC entry 1807 (class 2606 OID 16450)
-- Dependencies: 1519 1519
-- Name: financialmonth_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY financialmonth
    ADD CONSTRAINT financialmonth_pkey PRIMARY KEY (fmid);


--
-- TOC entry 1815 (class 2606 OID 16566)
-- Dependencies: 1522 1522
-- Name: financialoperation_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY financialoperation
    ADD CONSTRAINT financialoperation_pkey PRIMARY KEY (foid);


--
-- TOC entry 1811 (class 2606 OID 16515)
-- Dependencies: 1521 1521
-- Name: fo_spendings_finspid_key; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fo_spendings_finspid_key UNIQUE (finspid);


--
-- TOC entry 1813 (class 2606 OID 16513)
-- Dependencies: 1521 1521 1521
-- Name: fo_spendings_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fo_spendings_pkey PRIMARY KEY (foid, finspid);


--
-- TOC entry 1801 (class 2606 OID 16430)
-- Dependencies: 1516 1516
-- Name: manager_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY manager
    ADD CONSTRAINT manager_pkey PRIMARY KEY (managerid);


--
-- TOC entry 1805 (class 2606 OID 16445)
-- Dependencies: 1518 1518
-- Name: monthspending_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY monthspending
    ADD CONSTRAINT monthspending_pkey PRIMARY KEY (monthspid);


--
-- TOC entry 1809 (class 2606 OID 16471)
-- Dependencies: 1520 1520
-- Name: spending_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY spending
    ADD CONSTRAINT spending_pkey PRIMARY KEY (finspid);


--
-- TOC entry 1803 (class 2606 OID 16440)
-- Dependencies: 1517 1517
-- Name: spendingtemplate_pkey; Type: CONSTRAINT; Schema: public; Owner: director; Tablespace: 
--

ALTER TABLE ONLY spendingtemplate
    ADD CONSTRAINT spendingtemplate_pkey PRIMARY KEY (stid);


--
-- TOC entry 1818 (class 2606 OID 16567)
-- Dependencies: 1518 1522 1804
-- Name: fk9465defe2439d7a6; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY financialoperation
    ADD CONSTRAINT fk9465defe2439d7a6 FOREIGN KEY (monthspid) REFERENCES monthspending(monthspid);


--
-- TOC entry 1819 (class 2606 OID 16572)
-- Dependencies: 1522 1800 1516
-- Name: fk9465defe8dfee93f; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY financialoperation
    ADD CONSTRAINT fk9465defe8dfee93f FOREIGN KEY (managerid) REFERENCES manager(managerid);


--
-- TOC entry 1817 (class 2606 OID 16577)
-- Dependencies: 1521 1814 1522
-- Name: fkf4b0e7394c503038; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fkf4b0e7394c503038 FOREIGN KEY (foid) REFERENCES financialoperation(foid);


--
-- TOC entry 1816 (class 2606 OID 16526)
-- Dependencies: 1521 1520 1808
-- Name: fkf4b0e739e470c5bd; Type: FK CONSTRAINT; Schema: public; Owner: director
--

ALTER TABLE ONLY fo_spendings
    ADD CONSTRAINT fkf4b0e739e470c5bd FOREIGN KEY (finspid) REFERENCES spending(finspid);


--
-- TOC entry 1831 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- TOC entry 1832 (class 0 OID 0)
-- Dependencies: 1519
-- Name: financialmonth; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE financialmonth FROM PUBLIC;
REVOKE ALL ON TABLE financialmonth FROM director;
GRANT ALL ON TABLE financialmonth TO director;
GRANT ALL ON TABLE financialmonth TO "armDirectors";
GRANT SELECT ON TABLE financialmonth TO "armUsers";


--
-- TOC entry 1833 (class 0 OID 0)
-- Dependencies: 1522
-- Name: financialoperation; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE financialoperation FROM PUBLIC;
REVOKE ALL ON TABLE financialoperation FROM director;
GRANT ALL ON TABLE financialoperation TO director;
GRANT ALL ON TABLE financialoperation TO "armDirectors";
GRANT ALL ON TABLE financialoperation TO "armUsers";


--
-- TOC entry 1834 (class 0 OID 0)
-- Dependencies: 1521
-- Name: fo_spendings; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE fo_spendings FROM PUBLIC;
REVOKE ALL ON TABLE fo_spendings FROM director;
GRANT ALL ON TABLE fo_spendings TO director;
GRANT ALL ON TABLE fo_spendings TO "armDirectors";
GRANT ALL ON TABLE fo_spendings TO "armUsers";


--
-- TOC entry 1836 (class 0 OID 0)
-- Dependencies: 1515
-- Name: hibernate_sequence; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON SEQUENCE hibernate_sequence FROM PUBLIC;
REVOKE ALL ON SEQUENCE hibernate_sequence FROM director;
GRANT ALL ON SEQUENCE hibernate_sequence TO director;
GRANT ALL ON SEQUENCE hibernate_sequence TO "armDirectors";
GRANT ALL ON SEQUENCE hibernate_sequence TO "armUsers";


--
-- TOC entry 1837 (class 0 OID 0)
-- Dependencies: 1516
-- Name: manager; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE manager FROM PUBLIC;
REVOKE ALL ON TABLE manager FROM director;
GRANT ALL ON TABLE manager TO director;
GRANT ALL ON TABLE manager TO "armDirectors";
GRANT SELECT ON TABLE manager TO "armUsers";


--
-- TOC entry 1838 (class 0 OID 0)
-- Dependencies: 1518
-- Name: monthspending; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE monthspending FROM PUBLIC;
REVOKE ALL ON TABLE monthspending FROM director;
GRANT ALL ON TABLE monthspending TO director;
GRANT ALL ON TABLE monthspending TO "armDirectors";
GRANT SELECT ON TABLE monthspending TO "armUsers";


--
-- TOC entry 1839 (class 0 OID 0)
-- Dependencies: 1520
-- Name: spending; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE spending FROM PUBLIC;
REVOKE ALL ON TABLE spending FROM director;
GRANT ALL ON TABLE spending TO director;
GRANT ALL ON TABLE spending TO "armDirectors";
GRANT ALL ON TABLE spending TO "armUsers";


--
-- TOC entry 1840 (class 0 OID 0)
-- Dependencies: 1517
-- Name: spendingtemplate; Type: ACL; Schema: public; Owner: director
--

REVOKE ALL ON TABLE spendingtemplate FROM PUBLIC;
REVOKE ALL ON TABLE spendingtemplate FROM director;
GRANT ALL ON TABLE spendingtemplate TO director;
GRANT ALL ON TABLE spendingtemplate TO "armDirectors";
GRANT SELECT ON TABLE spendingtemplate TO "armUsers";


--
-- TOC entry 1178 (class 826 OID 16584)
-- Name: DEFAULT PRIVILEGES FOR TABLES; Type: DEFAULT ACL; Schema: -; Owner: postgres
--

ALTER DEFAULT PRIVILEGES FOR ROLE postgres REVOKE ALL ON TABLES  FROM PUBLIC;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres REVOKE ALL ON TABLES  FROM postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TABLES  TO postgres;
ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TABLES  TO "armDirectors";
ALTER DEFAULT PRIVILEGES FOR ROLE postgres GRANT ALL ON TABLES  TO "armUsers";


-- Completed on 2011-03-13 18:55:07

--
-- PostgreSQL database dump complete
--

