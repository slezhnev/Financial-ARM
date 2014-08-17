CREATE ROLE "armViewers"
  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;
--
REVOKE ALL ON TABLE financialmonth FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE financialmonth TO "armViewers";
--
REVOKE ALL ON TABLE financialoperation FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE financialoperation TO "armViewers";
--
REVOKE ALL ON TABLE fo_incomings FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE fo_incomings TO "armViewers";
--
REVOKE ALL ON TABLE fo_spendings FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE fo_spendings TO "armViewers";
--
REVOKE ALL ON TABLE incoming FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE incoming TO "armViewers";
--
REVOKE ALL ON TABLE manager FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE manager TO "armViewers";
--
REVOKE ALL ON TABLE managerpermonth FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE managerpermonth TO "armViewers";
--
REVOKE ALL ON TABLE monthspending FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE monthspending TO "armViewers";
--
REVOKE ALL ON TABLE spending FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE spending TO "armViewers";
--
REVOKE ALL ON TABLE spendingtemplate FROM "armViewers";
GRANT SELECT, REFERENCES, TRIGGER ON TABLE spendingtemplate TO "armViewers";
