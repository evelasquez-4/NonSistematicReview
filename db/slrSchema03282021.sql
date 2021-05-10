--
-- PostgreSQL database dump
--

-- Dumped from database version 11.11
-- Dumped by pg_dump version 13.1

-- Started on 2021-03-28 11:31:51 EDT

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE dbslr;
--
-- TOC entry 3499 (class 1262 OID 16386)
-- Name: dbslr; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE dbslr WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE = 'en_US.UTF-8';


ALTER DATABASE dbslr OWNER TO postgres;

\connect dbslr

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 9 (class 2615 OID 23172)
-- Name: slr; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA slr;


ALTER SCHEMA slr OWNER TO postgres;

--
-- TOC entry 3500 (class 0 OID 0)
-- Dependencies: 9
-- Name: SCHEMA slr; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA slr IS 'SLR schema';


--
-- TOC entry 286 (class 1255 OID 24829)
-- Name: slr_author_iud(character varying, integer, character varying, character varying, integer); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_author_iud(procedimiento character varying, auth_id integer, home_page character varying, author_name character varying, depto_id integer, OUT author_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$BEGIN
	IF procedimiento = 'AUTH_INS' THEN
	
		IF NOT EXISTS (SELECT 1 
					   FROM slr.authors aut 
					   WHERE aut.names = author_name )
		THEN
		
			INSERT INTO slr.authors(names,home_page,department_id) VALUES(author_name,home_page, depto_id);
			select  max(id) ::integer into author_id 
			from slr.authors;
			
		ELSE  
			select aut.id::integer into author_id 
			from slr.authors  aut
			where aut.names = author_name;
		END IF;
		
	ELSIF procedimiento = 'AUTH_INS_OTHER' THEN
		author_id = 0;
		IF NOT EXISTS (SELECT 1 
					   FROM slr.authors aut 
					   WHERE aut.names = author_name )
		THEN
			INSERT INTO slr.authors(names,home_page,department_id) VALUES(author_name,home_page, depto_id);
		END IF;
	
	ELSIF procedimiento = 'SEARCH_AUTHOR' THEN
		author_id = -1;
	
		select id into author_id 
		from slr.authors aut where aut.names = author_name;
		
		if author_id < 0 then
			raise exception 'Autor no registrado en slr.authors: %', author_name; 
		end if;
		
	END IF;

END;$$;


ALTER FUNCTION slr.slr_author_iud(procedimiento character varying, auth_id integer, home_page character varying, author_name character varying, depto_id integer, OUT author_id integer) OWNER TO postgres;

--
-- TOC entry 288 (class 1255 OID 24826)
-- Name: slr_author_publication_iud(character varying, integer, integer, character varying, integer, integer); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_author_publication_iud(accion character varying, author_id integer, publication_id integer, publication_type character varying, limite integer, herarchy integer, OUT res character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$declare
grupo_id integer;
pub record;
auth record; 
public_id integer;
auth_id integer; 
depto_id integer;
query_sql text := 'SELECT *
				   FROM slr.dblp_publication p
				   WHERE p.doc_type = '''|| publication_type || 
				   '''  AND p.updated_state = ''2.authors_processed''  
				   AND p.grupo = (	select group_state
									from public.slr_configurations 
									where state = ''activo''  )  ';

begin 
	--@params : accion , publication_type
	 if limite is not null AND limite > 0 then
			query_sql = query_sql||' LIMIT '||limite||';' ;
	 end if;
	 
	 select group_state into grupo_id 
	 from public.slr_configurations where state = 'activo';
	 
	 select dep.id into depto_id from slr.departments dep  where dep.id = 0; 
	 select aut.id into auth_id  from slr.authors aut where aut.id = 0; 
	 
	 if accion = 'AUTHPUB_INS'then
	 
		for pub IN EXECUTE query_sql
		loop
			public_id = slr.slr_publication_iud('PUB_INS'::text,null,pub.title,pub.key_dblp,
						pub.year::integer,pub.url,pub.ee,pub.note,pub.crossref,
						pub.mdate::date,'1.inserted',pub.doc_type);
			
			if (pub.authors::text = '{}')
			then
				select id into auth_id
				from slr.authors where id = 0;
				
				INSERT INTO slr.author_publications(herarchy,author_id,publication_id) 
				VALUES(0,auth_id,public_id);
				
			else 
			
				for auth in (select * from json_each_text(pub.authors) )
				loop

					auth_id = slr.slr_author_iud('SEARCH_AUTHOR',null,null,auth.value,null);

					--insert author_publications
					--raise notice ' autor %, public %',id_author,id_publication;
					INSERT INTO slr.author_publications(herarchy,author_id,publication_id) 
					VALUES(auth.key::INTEGER,auth_id,public_id);

				end loop;
				
			end if;
			--update row in public.dblp_publications
			-- 2.authors_processed -> 3.process
			update slr.dblp_publication set	updated_state = '3.finalize' where id = pub.id;
			
		end loop;
		
	end if;
	res = 'success';
end;$$;


ALTER FUNCTION slr.slr_author_publication_iud(accion character varying, author_id integer, publication_id integer, publication_type character varying, limite integer, herarchy integer, OUT res character varying) OWNER TO postgres;

--
-- TOC entry 292 (class 1255 OID 25805)
-- Name: slr_author_publications_ins(character varying, integer, integer, integer, integer, integer, character varying); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_author_publications_ins(action character varying, id integer, herarchy integer, author_id integer, publication_id integer, dblp_id integer, author_ids character varying, OUT publication integer, OUT publisher integer, OUT edition integer) RETURNS record
    LANGUAGE plpgsql
    AS $$
DECLARE
default_auth integer; 
default_depto integer;
auth_id integer;
pub record;
auth record; 
pub_id integer;
--publisher and edition variables
journal integer;
conference integer;

BEGIN 
	select a.id into default_auth from slr.authors a where a.id = 0;
	select d.id into default_depto from slr.departments d where d.id = 0;
	
	IF action = 'AUTHPUB_INS'
	THEN
		select * into pub 
		from  slr.dblp_publication d
		where d.id = dblp_id AND d.updated_state = '2.authors_processed';
	
		publication := slr.slr_publication_iud('PUB_INS'::text,null,pub.title,pub.key_dblp,
						pub.year::integer,pub.url,pub.ee,pub.note,pub.crossref,
						pub.mdate::date,'1.inserted',pub.doc_type);
		
		if (pub.authors::text = '{}')
		then 
			
			INSERT INTO slr.author_publications(herarchy,author_id,publication_id) 
			VALUES(0,default_auth,publication);
				
		else
			/*
			for auth in (select * from json_each_text(pub.authors) )
			loop
				
				auth_id = slr.slr_author_iud('SEARCH_AUTHOR',null,null,auth.value,null);
				INSERT INTO slr.author_publications(herarchy,author_id,publication_id) 
				VALUES(auth.key::INTEGER,auth_id,publication);
				
			end loop;
			*/
			FOR auth IN ( 
							SELECT row_number() OVER ( ORDER BY (SELECT 0) ) AS sequencia,split.partes as author_names
   						    FROM ( SELECT regexp_split_to_table(author_ids,',') AS partes ) split
						)
			LOOP
				--RAISE NOTICE 'ID1->%,ID2->%',auth.sequencia,auth.author_names;
				INSERT INTO slr.author_publications(herarchy,author_id,publication_id) 
				VALUES(auth.sequencia::INTEGER,auth.author_names::INTEGER,publication);
			END LOOP;
			
		end if;
		--update row in public.dblp_publications
		-- 2.authors_processed -> 3.process
		update slr.dblp_publication set	updated_state = '3.finalize' where slr.dblp_publication.id = pub.id;
	
		--publisher, edition inserts
		publisher :=  slr.slr_publisher_ins('PUB_INS',null,pub.publisher);
		journal := slr.slr_publisher_ins('JOUR_INS',null,pub.journal);
		conference := slr.slr_publisher_ins('CONF_INS',null,pub.book_title);
		
		INSERT INTO slr.editions(description,volume,number,publisher_id,conference_id,journal_id)
		VALUES('Edition Desc: volume'||pub.volume||', number '||pub.number,
			  pub.volume,pub.number,publisher,conference,journal) RETURNING slr.editions.id INTO edition;
		
	RETURN;		
	
	END IF;

END;
$$;


ALTER FUNCTION slr.slr_author_publications_ins(action character varying, id integer, herarchy integer, author_id integer, publication_id integer, dblp_id integer, author_ids character varying, OUT publication integer, OUT publisher integer, OUT edition integer) OWNER TO postgres;

--
-- TOC entry 289 (class 1255 OID 25139)
-- Name: slr_default_values(); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_default_values() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
declare 
res character;
max_value integer;
statements CURSOR FOR
	    SELECT tablename FROM pg_tables p
        WHERE p.tableowner = 'postgres' AND p.schemaname = 'slr'
		AND p.tablename IN (
			'countries', 'institutions','departments',
			'authors','keywords','publishers',
			'conferences','editions','journals'
		);

begin 
	--country
	IF NOT EXISTS(SELECT 1 FROM slr.countries WHERE id = 0) THEN
		INSERT INTO slr.countries(id,country_name,code) VALUES(0,'DEFAULT','DEF');
	END IF;
	select id into max_value from slr.countries where id = 0;
	
	--institution
	IF NOT EXISTS(SELECT 1 FROM slr.institutions WHERE id = 0) THEN
		INSERT INTO slr.institutions(id,country_id,description) VALUES(0,max_value,'DEFAULT');
	END IF;
	--department
	if not EXISTS(SELECT 1 FROM slr.departments WHERE id = 0) then
		--SELECT id into max_value FROM slr.institution WHERE id = 0;
		INSERT INTO slr.departments(id,description,institution_id) VALUES(0,'DEFAULT',0);
	end if;
	--author
	--if not EXISTS(SELECT 1 FROM slr.authors WHERE id = 0) then
	--	INSERT INTO slr.authors(id,names,department_id,home_page) VALUES(0,'DEFAULT',max_value,'DEFAULT');
	--end if;
	--keyword
	if not exists(select 1 from slr.keywords where id = 0) then
		insert into slr.keywords(id,decription) values(0,'DEFAULT');
	end if;
	--publisher
	if not exists(select 1 from slr.publishers where id=0) then
		insert into slr.publishers(id,description,state) values(0,'DEFAULT','active');
	end if;
	--conference
	if not EXISTS(select 1 from slr.conferences where id = 0) then
		insert into slr.conferences(id,description,abreviation) values(0,'DEFAULT','DEFAULT');
	end if;
	--journal
	if not exists(select 1 from slr.journals where id = 0) then 
		insert into slr.journals(id,name,abreviation) values(0,'DEFAULT','DEFAULT');
	end if;
	--edition
	if not exists(select 1 from slr.editions where id = 0)then
		insert into slr.editions(id,description,volume,number,publisher_id,conference_id,journal_id) 
		values(0,'DEFAULT','','',max_value,max_value,max_value);
	end if;
	--authors
	if not exists(select 1 from slr.authors where id = 0) then
		insert into slr.authors(id, names, email, "position", home_page, department_id, skills, disciplines, created_at)
		VALUES(0,'','DEFAULT EMAIL','','DEFAULT HOME PAGE',0,'','',DEFAULT);
	end if;
	
return 'default values inserted';
end;
$$;


ALTER FUNCTION slr.slr_default_values() OWNER TO postgres;

--
-- TOC entry 291 (class 1255 OID 25678)
-- Name: slr_edition_iud(character varying, integer, integer); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_edition_iud(action character varying, publisher_id integer, dblp_id integer, OUT edition_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$DECLARE
dblp record;
publisher record;
conference record;
journal record;
conf_id integer;
jour_id integer;
BEGIN 
	SELECT * INTO dblp FROM slr.dblp_publication  WHERE id = dblp_id;	
	SELECT * INTO publisher FROM slr.publishers WHERE id = publisher_id;	
	
	IF action = 'EDIT_INS' THEN
	
		IF dblp.volume = '' AND dblp.number = '' THEN
		
			CASE WHEN dblp.journal = '' OR dblp.journal IS NULL  THEN
				SELECT id INTO jour_id FROM slr.journals WHERE id = 0;
			ELSE
				--IF NOT EXISTS(SELECT 1 FROM slr.journals jor WHERE LOWER(jor.name) = LOWER(dblp.journal) ) THEN
					INSERT INTO slr.journals(id,name, abreviation) VALUES(default,dblp.journal,'') RETURNING id INTO jour_id;				
				--ELSE
					--SELECT id INTO jour_id FROM slr.journals jor WHERE LOWER(jor.name) = LOWER(dblp.journal);
				--END IF; 
			END CASE;
				
			IF dblp.doc_type = 'book' OR dblp.doc_type = 'incollection' THEN
				SELECT id INTO conf_id FROM slr.conferences WHERE id = 0;
			ELSE
				CASE WHEN dblp.book_title = '' OR dblp.book_title IS NULL THEN
					--RAISE NOTICE 'default conference';
					--SELECT * INTO conference FROM slr.conferences WHERE id = 0;
					SELECT id INTO conf_id FROM slr.conferences WHERE id = 0;
				ELSE
					--IF NOT EXISTS ( SELECT 1 FROM slr.conferences WHERE LOWER(description) = LOWER(dblp.book_title) )
					--THEN
						INSERT INTO slr.conferences(id,description,abreviation) VALUES(default,dblp.book_title,dblp.title) RETURNING id INTO conf_id;
					--ELSE
						--SELECT id INTO conf_id FROM slr.conferences WHERE LOWER(description) = LOWER(dblp.book_title); 
					--END IF;
				END CASE;
			END IF;
			
			--EDITIONS
			IF( publisher_id = 0 AND jour_id = 0 AND conf_id = 0) THEN
				SELECT id INTO edition_id FROM slr.editions WHERE id = 0; --DEFAULT EDITIONS id->0
			ELSE
				INSERT INTO slr.editions(id,description,volume,number,publisher_id,conference_id,journal_id)
				VALUES(default,'','','',publisher_id,conf_id,jour_id) RETURNING id INTO edition_id;
			END IF;
		--Inicio dblp.volume OR dblp.number <> ''
		ELSE
			--IF NOT EXISTS(	SELECT 1 FROM slr.editions edit
			--			  	WHERE LOWER(edit.volume)=LOWER(dblp.volume) AND LOWER(edit.number)=LOWER(dblp.number) 
			--			  	AND edit.publisher_id=$2) 
			--THEN
				IF dblp.doc_type = 'book' OR dblp.doc_type = 'incollection' THEN
					SELECT id INTO jour_id FROM slr.journals WHERE id = 0;
					SELECT id INTO conf_id FROM slr.conferences WHERE id = 0;
				ELSE
					IF dblp.journal = '' OR dblp.journal IS NULL THEN
						SELECT id INTO jour_id FROM slr.journals WHERE id = 0;
					ELSE
						--IF NOT EXISTS(SELECT 1 FROM slr.journals jor WHERE LOWER(jor.name) = LOWER(dblp.journal) ) THEN
							INSERT INTO slr.journals(id,name, abreviation) VALUES(default,dblp.journal,'') RETURNING id INTO jour_id;
						--ELSE
							--SELECT jor.id INTO jour_id FROM slr.journals jor WHERE LOWER(jor.name) = LOWER(dblp.journal);
						--END IF;
					END IF;
					
					IF dblp.book_title = '' OR dblp.book_title IS NULL THEN
						SELECT id INTO conf_id FROM slr.conferences WHERE id = 0;
					ELSE
						--IF NOT EXISTS ( SELECT 1 FROM slr.conferences WHERE LOWER(description) = LOWER(dblp.book_title) )
						--THEN
							INSERT INTO slr.conferences(id,description,abreviation) VALUES(default,dblp.book_title,dblp.title) RETURNING id INTO conf_id;
						--ELSE
							--SELECT id INTO conf_id FROM slr.conferences WHERE LOWER(description) = LOWER(dblp.book_title); 
						--END IF;
					END IF;
					
				END IF;
			
			--ELSE
			--	SELECT id INTO edition_id 
			--	FROM slr.editions edit
			--	WHERE LOWER(edit.volume)=LOWER(dblp.volume) 
			--	AND LOWER(edit.number)=LOWER(dblp.number) AND edit.publisher_id=$2;
			--END IF;
			
			--EDITIONS
			IF( publisher_id = 0 AND jour_id = 0 AND conf_id = 0) THEN
				SELECT id INTO edition_id FROM slr.editions WHERE id = 0; --DEFAULT EDITIONS id->0
			ELSE
				INSERT INTO slr.editions(id,description,volume,number,publisher_id,conference_id,journal_id)
				VALUES(default,'Edition Desc: volume '||dblp.volume||', number '||dblp.number,dblp.volume,dblp.number,publisher_id,conf_id,jour_id) RETURNING id INTO edition_id;
			END IF;
			
		END IF;
	
	END IF;

END;$_$;


ALTER FUNCTION slr.slr_edition_iud(action character varying, publisher_id integer, dblp_id integer, OUT edition_id integer) OWNER TO postgres;

--
-- TOC entry 290 (class 1255 OID 23798)
-- Name: slr_process_authors(character varying, character varying, integer); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_process_authors(accion character varying, doc_type character varying, limite integer DEFAULT 0, OUT res character varying) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
rec record;
auth record;
grupo_ins integer;
author_id integer;
publication_id integer;
cadena text;
query_sql text := ' select dp.id,dp.authors
			 from slr.dblp_publication dp
			 where dp.updated_state = ''1.inserted'' 
			 and dp.doc_type = ''' ||doc_type|| '''
			 and dp.grupo = (select group_state 
							from public.slr_configurations c
							where c.state = ''activo'') 
			group by id ASC ';

total_editions integer := 0;
BEGIN 
	select group_state into grupo_ins from public.slr_configurations c where c.state = 'activo';
	
	IF accion = 'INSERT_NAMES' THEN
		if (limite is not null AND limite != 0 )then
			query_sql = query_sql||' limit '||limite||';' ;
		end if;
		
		cadena = 'CREATE TEMPORARY TABLE  temp_authors
						 ( 	nombres varchar(255) )  ON COMMIT DROP ;';
		EXECUTE(cadena);
		
		for rec in execute query_sql 
		loop
		
			for auth in select * from json_each_text(rec.authors)
			loop
				insert into temp_authors(nombres) values(auth.value);
			end loop;
			
			update slr.dblp_publication set updated_state = '2.authors_processed' where id = rec.id; 
			
		end loop;
		
		EXECUTE('CREATE TEMPORARY TABLE temp_tab2 ( nombres varchar(255) )  ON COMMIT DROP ;');
		INSERT INTO temp_tab2( select DISTINCT(nombres) from temp_authors);

		INSERT INTO slr.authors(	 select nextval('slr.authors_id_seq'), ta.nombres as names,
									   			'' as email,'' as position,
												'DEFAULT HOME PAGE' as home_page,
												0 as department_id,
												'' as skills,
												'' as disciplines
									   	  from temp_tab2 ta
									   	  where ta.nombres NOT IN(
										  		SELECT DISTINCT(aut.names)
												FROM slr.authors aut
												INNER JOIN temp_tab2 temp_aut 
												ON temp_aut.nombres = aut.names
										  )
									  );
	
	ELSIF accion = 'PROC_AUTHORS' THEN

		res = slr.slr_process_authors('INSERT_NAMES',doc_type,limite);
	END IF;

	res = 'success';
END;
$$;


ALTER FUNCTION slr.slr_process_authors(accion character varying, doc_type character varying, limite integer, OUT res character varying) OWNER TO postgres;

--
-- TOC entry 293 (class 1255 OID 25817)
-- Name: slr_publication_ins(character varying, character varying, character varying, character varying, integer, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, integer); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_publication_ins(action character varying, OUT pub_id integer, pub_abstract character varying, pub_title character varying, pub_dblpkey character varying, pub_year integer, pub_url character varying, pub_ee character varying, pub_note character varying, pub_crossref character varying, pub_mdate character varying, pub_doctype character varying, pub_proceeding_info character varying, pub_authors character varying, dblp_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
declare
authors record;
begin
	IF action = 'PUB_INS_DBLP' THEN
		--pub_id := slr.slr_publication_iud('PUB_INS',null,pub_title,pub_dblpkey,
		--				pub_year::integer,pub_url,pub_ee,pub_note,pub_crossref,
		--				pub_mdate::date,'1.inserted',pub_doctype);
		INSERT INTO slr.publications(abstract,title,dblp_key,year,url,ee,note,crossref,mdate,
									 updated_state,doc_type,proceeding_info)
		 VALUES('',pub_title,pub_dblpkey,pub_year::integer,pub_url,pub_ee,pub_note,pub_crossref,pub_mdate::date,
				'1.inserted',pub_doctype,pub_proceeding_info) RETURNING id INTO pub_id;
		
		if(pub_authors = '' OR pub_authors is null) then
			INSERT INTO slr.author_publications(herarchy,author_id,publication_id) 
			VALUES(0,0,pub_id);
		else
			FOR authors IN (
							SELECT row_number() OVER ( ORDER BY (SELECT 0) ) AS sequencia,split.partes as author_names
   						    FROM ( SELECT regexp_split_to_table(pub_authors,',') AS partes ) split
							) 
			LOOP
				--RAISE NOTICE 'ID1->%,ID2->%',auth.sequencia,auth.author_names;
				INSERT INTO slr.author_publications(herarchy,author_id,publication_id) 
				VALUES(authors.sequencia::INTEGER,authors.author_names::INTEGER,pub_id);
			END LOOP;
		end if;
		UPDATE slr.dblp_publication SET	updated_state = '3.finalize' WHERE slr.dblp_publication.id = dblp_id;
		
	END IF;
end;
$$;


ALTER FUNCTION slr.slr_publication_ins(action character varying, OUT pub_id integer, pub_abstract character varying, pub_title character varying, pub_dblpkey character varying, pub_year integer, pub_url character varying, pub_ee character varying, pub_note character varying, pub_crossref character varying, pub_mdate character varying, pub_doctype character varying, pub_proceeding_info character varying, pub_authors character varying, dblp_id integer) OWNER TO postgres;

--
-- TOC entry 284 (class 1255 OID 24828)
-- Name: slr_publication_iud(character varying, character varying, character varying, character varying, integer, character varying, character varying, character varying, character varying, date, character varying, character varying); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_publication_iud(action character varying, abstract character varying, title character varying, dblp_key character varying, year integer, url character varying, ee character varying, note character varying, crossref character varying, mdate date, updated_state character varying, doc_type character varying, OUT publication_id integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$begin
	if action = 'PUB_INS' then
	
		insert into slr.publications
		(abstract,title,dblp_key,year,url,ee,note,crossref,mdate,
		 updated_state,doc_type)
		 values(abstract,title,dblp_key,year,url,ee,note,crossref,mdate,
				updated_state,doc_type ) 
		 
		 RETURNING id INTO publication_id;
		
	end if;
end;$$;


ALTER FUNCTION slr.slr_publication_iud(action character varying, abstract character varying, title character varying, dblp_key character varying, year integer, url character varying, ee character varying, note character varying, crossref character varying, mdate date, updated_state character varying, doc_type character varying, OUT publication_id integer) OWNER TO postgres;

--
-- TOC entry 285 (class 1255 OID 25797)
-- Name: slr_publisher_ins(character varying, integer, character varying); Type: FUNCTION; Schema: slr; Owner: postgres
--

CREATE FUNCTION slr.slr_publisher_ins(action character varying, publisher_id integer, publisher_desc character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
response integer;
BEGIN
	IF action = 'PUB_INS' THEN
		INSERT INTO slr.publishers(id,description,state)
		VALUES(default,publisher_desc,'active') RETURNING id INTO response;
	ELSIF action = 'JOUR_INS' THEN
		if (publisher_desc is NULL OR publisher_desc = '' ) then
			SELECT id INTO response FROM slr.journals where id = 0;
		else
			INSERT INTO slr.journals(id,name,abreviation) VALUES(default,publisher_desc,'') RETURNING id INTO response;
		end if;
	ELSIF action = 'CONF_INS' THEN
		if (publisher_desc is NULL OR publisher_desc = '' ) then
			SELECT id INTO response FROM slr.conferences WHERE id = 0;
		else
			INSERT INTO slr.conferences(id,description,abreviation) VALUES(default,publisher_desc,'') RETURNING id INTO response;
		end if;
	END IF;
	
	RETURN response;
END;
$$;


ALTER FUNCTION slr.slr_publisher_ins(action character varying, publisher_id integer, publisher_desc character varying) OWNER TO postgres;

--
-- TOC entry 236 (class 1259 OID 23828)
-- Name: author_publications_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.author_publications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.author_publications_id_seq OWNER TO postgres;

SET default_tablespace = '';

--
-- TOC entry 237 (class 1259 OID 23830)
-- Name: author_publications; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.author_publications (
    id bigint DEFAULT nextval('slr.author_publications_id_seq'::regclass) NOT NULL,
    herarchy integer,
    author_id integer,
    publication_id integer,
    created_at date DEFAULT now()
);


ALTER TABLE slr.author_publications OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 23700)
-- Name: authors_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.authors_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.authors_id_seq OWNER TO postgres;

--
-- TOC entry 271 (class 1259 OID 50675)
-- Name: authors; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.authors (
    id bigint DEFAULT nextval('slr.authors_id_seq'::regclass) NOT NULL,
    key text,
    pid text,
    department_id integer,
    "position" text,
    skills text,
    disciplines text,
    names text NOT NULL,
    homonyns character varying[],
    urls character varying[],
    cites character varying[],
    created_at date DEFAULT now(),
    awards character varying[],
    affiliation text,
    mdate character varying(70),
    "group" integer
);


ALTER TABLE slr.authors OWNER TO postgres;

--
-- TOC entry 232 (class 1259 OID 23702)
-- Name: authors_ant; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.authors_ant (
    id bigint DEFAULT nextval('slr.authors_id_seq'::regclass) NOT NULL,
    names text NOT NULL,
    "position" text,
    key text,
    department_id integer,
    skills text,
    disciplines text,
    created_at date DEFAULT now(),
    mdate date DEFAULT now(),
    pid text,
    homonyns character varying[],
    urls character varying[],
    cites character varying[]
);


ALTER TABLE slr.authors_ant OWNER TO postgres;

--
-- TOC entry 267 (class 1259 OID 25163)
-- Name: books_chapters_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.books_chapters_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.books_chapters_id_seq OWNER TO postgres;

--
-- TOC entry 248 (class 1259 OID 23918)
-- Name: book_chapters; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.book_chapters (
    id bigint DEFAULT nextval('slr.books_chapters_id_seq'::regclass) NOT NULL,
    publication_id integer NOT NULL,
    publisher_id integer,
    cite text,
    chapter character varying(100),
    book_title text,
    pages character varying(100),
    isbn character varying(200),
    series text,
    school character varying(255),
    created_at date DEFAULT now()
);


ALTER TABLE slr.book_chapters OWNER TO postgres;

--
-- TOC entry 244 (class 1259 OID 23887)
-- Name: books; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.books (
    id bigint NOT NULL,
    publication_id integer NOT NULL,
    publisher_id integer,
    series character varying(255),
    book_title text,
    pages character varying(100),
    isbn text,
    school text,
    cite character varying(200),
    month character varying(80),
    note text,
    created_at date DEFAULT now()
);


ALTER TABLE slr.books OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 23885)
-- Name: books_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.books_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.books_id_seq OWNER TO postgres;

--
-- TOC entry 3501 (class 0 OID 0)
-- Dependencies: 243
-- Name: books_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.books_id_seq OWNED BY slr.books.id;


--
-- TOC entry 266 (class 1259 OID 24050)
-- Name: conference_editorials; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.conference_editorials (
    id bigint NOT NULL,
    publication_id integer,
    edition_id integer,
    isbn text,
    note text,
    series character varying(255),
    book_title text,
    pages character varying(100),
    created_at date DEFAULT now()
);


ALTER TABLE slr.conference_editorials OWNER TO postgres;

--
-- TOC entry 265 (class 1259 OID 24048)
-- Name: conference_editorials_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.conference_editorials_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.conference_editorials_id_seq OWNER TO postgres;

--
-- TOC entry 3502 (class 0 OID 0)
-- Dependencies: 265
-- Name: conference_editorials_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.conference_editorials_id_seq OWNED BY slr.conference_editorials.id;


--
-- TOC entry 264 (class 1259 OID 24040)
-- Name: conference_papers; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.conference_papers (
    id bigint NOT NULL,
    publication_id integer,
    edition_id integer,
    pages character varying(100),
    month character varying(80),
    cite character varying(200),
    note text,
    book_title text,
    created_at date DEFAULT now()
);


ALTER TABLE slr.conference_papers OWNER TO postgres;

--
-- TOC entry 263 (class 1259 OID 24038)
-- Name: conference_papers_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.conference_papers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.conference_papers_id_seq OWNER TO postgres;

--
-- TOC entry 3503 (class 0 OID 0)
-- Dependencies: 263
-- Name: conference_papers_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.conference_papers_id_seq OWNED BY slr.conference_papers.id;


--
-- TOC entry 255 (class 1259 OID 23981)
-- Name: conferences_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.conferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.conferences_id_seq OWNER TO postgres;

--
-- TOC entry 256 (class 1259 OID 23983)
-- Name: conferences; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.conferences (
    id bigint DEFAULT nextval('slr.conferences_id_seq'::regclass) NOT NULL,
    description text,
    abreviation text,
    created_at date DEFAULT now()
);


ALTER TABLE slr.conferences OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 23783)
-- Name: countries_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.countries_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.countries_id_seq OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 23475)
-- Name: countries; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.countries (
    id bigint DEFAULT nextval('slr.countries_id_seq'::regclass) NOT NULL,
    country_name character varying(255) NOT NULL,
    code character varying(5),
    created_at date DEFAULT now()
);


ALTER TABLE slr.countries OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 23253)
-- Name: dblp_publication_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.dblp_publication_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.dblp_publication_id_seq OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 23190)
-- Name: dblp_publication; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.dblp_publication (
    id integer DEFAULT nextval('slr.dblp_publication_id_seq'::regclass) NOT NULL,
    key_dblp character varying(200),
    authors json,
    doc_type character varying(100),
    editor character varying(100),
    pages character varying(100),
    year integer,
    title text,
    address text,
    journal text,
    volume character varying(100),
    number character varying(100),
    month character varying(100),
    url text,
    ee text,
    cdrom text,
    cite text,
    publisher text,
    note text,
    crossref text,
    isbn text,
    series text,
    school text,
    chapter text,
    publnr text,
    book_title text,
    mdate character varying(100),
    reg_date date DEFAULT now(),
    updated_state character varying(100) DEFAULT '1.inserted'::character varying,
    grupo integer DEFAULT 0,
    proceeding_info character varying(100) DEFAULT NULL::character varying
);


ALTER TABLE slr.dblp_publication OWNER TO postgres;

--
-- TOC entry 269 (class 1259 OID 34212)
-- Name: dblp_publications_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.dblp_publications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.dblp_publications_id_seq OWNER TO postgres;

--
-- TOC entry 270 (class 1259 OID 50609)
-- Name: dblp_publications; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.dblp_publications (
    id bigint DEFAULT nextval('slr.dblp_publications_id_seq'::regclass) NOT NULL,
    key_dblp character varying(250),
    author character varying[],
    title character varying,
    book_title text,
    pages character varying(100),
    year integer,
    crossref text,
    address text,
    journal text,
    volume character varying(100),
    number character varying(100),
    month character varying(100),
    url text,
    ee text,
    cdrom text,
    cite text,
    publisher text,
    note text,
    isbn text,
    series text,
    school text,
    chapter text,
    publnr text,
    mdate character varying(100),
    reg_date date DEFAULT now(),
    updated_state character varying(100) DEFAULT '1.inserted'::character varying,
    grupo integer DEFAULT 0,
    doc_type character varying(100)
);


ALTER TABLE slr.dblp_publications OWNER TO postgres;

--
-- TOC entry 3504 (class 0 OID 0)
-- Dependencies: 270
-- Name: COLUMN dblp_publications.pages; Type: COMMENT; Schema: slr; Owner: postgres
--

COMMENT ON COLUMN slr.dblp_publications.pages IS 'format : from - to
last page unkown: from -
single page: number
split articles: number, number';


--
-- TOC entry 3505 (class 0 OID 0)
-- Dependencies: 270
-- Name: COLUMN dblp_publications.year; Type: COMMENT; Schema: slr; Owner: postgres
--

COMMENT ON COLUMN slr.dblp_publications.year IS 'four digits number';


--
-- TOC entry 3506 (class 0 OID 0)
-- Dependencies: 270
-- Name: COLUMN dblp_publications.crossref; Type: COMMENT; Schema: slr; Owner: postgres
--

COMMENT ON COLUMN slr.dblp_publications.crossref IS 'reference to other dblp key.
improcedings  to proceeding
journal to proceeding
incollection to collection
proceeding to book';


--
-- TOC entry 229 (class 1259 OID 23652)
-- Name: departments_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.departments_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.departments_id_seq OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 23682)
-- Name: departments; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.departments (
    id bigint DEFAULT nextval('slr.departments_id_seq'::regclass) NOT NULL,
    description text,
    "position" character varying(255),
    skills text,
    institution_id integer,
    created_at date DEFAULT now(),
    link character varying(255) DEFAULT NULL::character varying,
    validate boolean DEFAULT false,
    members integer DEFAULT 0
);


ALTER TABLE slr.departments OWNER TO postgres;

--
-- TOC entry 250 (class 1259 OID 23944)
-- Name: editions; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.editions (
    id bigint NOT NULL,
    description text,
    volume character varying(255) DEFAULT NULL::character varying,
    number character varying(255) DEFAULT NULL::character varying,
    publisher_id integer,
    conference_id integer,
    journal_id integer
);


ALTER TABLE slr.editions OWNER TO postgres;

--
-- TOC entry 249 (class 1259 OID 23942)
-- Name: editions_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.editions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.editions_id_seq OWNER TO postgres;

--
-- TOC entry 3507 (class 0 OID 0)
-- Dependencies: 249
-- Name: editions_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.editions_id_seq OWNED BY slr.editions.id;


--
-- TOC entry 247 (class 1259 OID 23916)
-- Name: incollections_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.incollections_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.incollections_id_seq OWNER TO postgres;

--
-- TOC entry 3508 (class 0 OID 0)
-- Dependencies: 247
-- Name: incollections_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.incollections_id_seq OWNED BY slr.book_chapters.id;


--
-- TOC entry 227 (class 1259 OID 23625)
-- Name: institutions_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.institutions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.institutions_id_seq OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 23627)
-- Name: institutions; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.institutions (
    id bigint DEFAULT nextval('slr.institutions_id_seq'::regclass) NOT NULL,
    description text,
    country_id integer,
    created_at date DEFAULT now(),
    updated boolean DEFAULT false,
    link character varying(255)
);


ALTER TABLE slr.institutions OWNER TO postgres;

--
-- TOC entry 260 (class 1259 OID 24025)
-- Name: journal_editorials; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.journal_editorials (
    id bigint NOT NULL,
    publication_id integer,
    edition_id integer,
    series character varying(255),
    isbn text,
    book_title text,
    created_at date DEFAULT now(),
    note text,
    pages character varying(100)
);


ALTER TABLE slr.journal_editorials OWNER TO postgres;

--
-- TOC entry 259 (class 1259 OID 24023)
-- Name: journal_editorial_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.journal_editorial_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.journal_editorial_id_seq OWNER TO postgres;

--
-- TOC entry 3509 (class 0 OID 0)
-- Dependencies: 259
-- Name: journal_editorial_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.journal_editorial_id_seq OWNED BY slr.journal_editorials.id;


--
-- TOC entry 262 (class 1259 OID 24033)
-- Name: journal_papers; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.journal_papers (
    id bigint NOT NULL,
    publication_id integer,
    edition_id integer,
    pages character varying(100),
    month character varying(80),
    cite character varying(200),
    note text,
    book_title text,
    created_at date DEFAULT now()
);


ALTER TABLE slr.journal_papers OWNER TO postgres;

--
-- TOC entry 261 (class 1259 OID 24031)
-- Name: journal_papers_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.journal_papers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.journal_papers_id_seq OWNER TO postgres;

--
-- TOC entry 3510 (class 0 OID 0)
-- Dependencies: 261
-- Name: journal_papers_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.journal_papers_id_seq OWNED BY slr.journal_papers.id;


--
-- TOC entry 257 (class 1259 OID 23993)
-- Name: journals_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.journals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.journals_id_seq OWNER TO postgres;

--
-- TOC entry 258 (class 1259 OID 23995)
-- Name: journals; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.journals (
    id bigint DEFAULT nextval('slr.journals_id_seq'::regclass) NOT NULL,
    name text,
    abreviation character varying(255),
    created_at date DEFAULT now()
);


ALTER TABLE slr.journals OWNER TO postgres;

--
-- TOC entry 251 (class 1259 OID 23953)
-- Name: keywords_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.keywords_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.keywords_id_seq OWNER TO postgres;

--
-- TOC entry 252 (class 1259 OID 23955)
-- Name: keywords; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.keywords (
    id bigint DEFAULT nextval('slr.keywords_id_seq'::regclass) NOT NULL,
    decription character varying(255),
    created_at date DEFAULT now()
);


ALTER TABLE slr.keywords OWNER TO postgres;

--
-- TOC entry 253 (class 1259 OID 23962)
-- Name: publication_keywords_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.publication_keywords_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.publication_keywords_id_seq OWNER TO postgres;

--
-- TOC entry 254 (class 1259 OID 23964)
-- Name: publication_keywords; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.publication_keywords (
    id bigint DEFAULT nextval('slr.publication_keywords_id_seq'::regclass) NOT NULL,
    keyword_id integer,
    publication_id integer,
    created_at date DEFAULT now()
);


ALTER TABLE slr.publication_keywords OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 23788)
-- Name: publications; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.publications (
    id bigint NOT NULL,
    abstract text DEFAULT ''::text,
    title text,
    dblp_key character varying(100),
    year integer,
    url text,
    ee text,
    note text,
    crossref text,
    mdate date,
    updated_state character varying(80),
    doc_type character varying(80),
    reg_date date DEFAULT now(),
    rg_info text DEFAULT ''::text,
    proceeding_info character varying(100) DEFAULT NULL::character varying
);


ALTER TABLE slr.publications OWNER TO postgres;

--
-- TOC entry 3511 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN publications.rg_info; Type: COMMENT; Schema: slr; Owner: postgres
--

COMMENT ON COLUMN slr.publications.rg_info IS 'campos extras de researchgate';


--
-- TOC entry 234 (class 1259 OID 23786)
-- Name: publications_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.publications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.publications_id_seq OWNER TO postgres;

--
-- TOC entry 3512 (class 0 OID 0)
-- Dependencies: 234
-- Name: publications_id_seq; Type: SEQUENCE OWNED BY; Schema: slr; Owner: postgres
--

ALTER SEQUENCE slr.publications_id_seq OWNED BY slr.publications.id;


--
-- TOC entry 245 (class 1259 OID 23898)
-- Name: publishers_id_seq; Type: SEQUENCE; Schema: slr; Owner: postgres
--

CREATE SEQUENCE slr.publishers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE slr.publishers_id_seq OWNER TO postgres;

--
-- TOC entry 246 (class 1259 OID 23900)
-- Name: publishers; Type: TABLE; Schema: slr; Owner: postgres
--

CREATE TABLE slr.publishers (
    id bigint DEFAULT nextval('slr.publishers_id_seq'::regclass) NOT NULL,
    description text,
    state character varying(200) DEFAULT 'active'::character varying,
    created_at date DEFAULT now()
);


ALTER TABLE slr.publishers OWNER TO postgres;

--
-- TOC entry 240 (class 1259 OID 23862)
-- Name: view_articles; Type: VIEW; Schema: slr; Owner: postgres
--

CREATE VIEW slr.view_articles AS
 SELECT p.id,
    p.key_dblp,
    p.authors,
    p.doc_type,
    p.editor,
    p.pages,
    p.year,
    p.title,
    p.address,
    p.journal,
    p.volume,
    p.number,
    p.month,
    p.url,
    p.ee,
    p.cdrom,
    p.cite,
    p.publisher,
    p.note,
    p.crossref,
    p.isbn,
    p.series,
    p.school,
    p.chapter,
    p.publnr,
    p.book_title,
    p.mdate,
    p.reg_date,
    p.updated_state,
    p.grupo
   FROM slr.dblp_publication p
  WHERE ((p.doc_type)::text = 'article'::text);


ALTER TABLE slr.view_articles OWNER TO postgres;

--
-- TOC entry 238 (class 1259 OID 23847)
-- Name: view_books; Type: VIEW; Schema: slr; Owner: postgres
--

CREATE VIEW slr.view_books AS
 SELECT dblp_publication.id,
    dblp_publication.key_dblp,
    dblp_publication.authors,
    dblp_publication.doc_type,
    dblp_publication.editor,
    dblp_publication.pages,
    dblp_publication.year,
    dblp_publication.title,
    dblp_publication.address,
    dblp_publication.journal,
    dblp_publication.volume,
    dblp_publication.number,
    dblp_publication.month,
    dblp_publication.url,
    dblp_publication.ee,
    dblp_publication.cdrom,
    dblp_publication.cite,
    dblp_publication.publisher,
    dblp_publication.note,
    dblp_publication.crossref,
    dblp_publication.isbn,
    dblp_publication.series,
    dblp_publication.school,
    dblp_publication.chapter,
    dblp_publication.publnr,
    dblp_publication.book_title,
    dblp_publication.mdate,
    dblp_publication.reg_date,
    dblp_publication.updated_state,
    dblp_publication.grupo
   FROM slr.dblp_publication
  WHERE ((dblp_publication.doc_type)::text = 'book'::text);


ALTER TABLE slr.view_books OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 23857)
-- Name: view_incollections; Type: VIEW; Schema: slr; Owner: postgres
--

CREATE VIEW slr.view_incollections AS
 SELECT p.id,
    p.key_dblp,
    p.authors,
    p.doc_type,
    p.editor,
    p.pages,
    p.year,
    p.title,
    p.address,
    p.journal,
    p.volume,
    p.number,
    p.month,
    p.url,
    p.ee,
    p.cdrom,
    p.cite,
    p.publisher,
    p.note,
    p.crossref,
    p.isbn,
    p.series,
    p.school,
    p.chapter,
    p.publnr,
    p.book_title,
    p.mdate,
    p.reg_date,
    p.updated_state,
    p.grupo
   FROM slr.dblp_publication p
  WHERE ((p.doc_type)::text = 'incollection'::text);


ALTER TABLE slr.view_incollections OWNER TO postgres;

--
-- TOC entry 242 (class 1259 OID 23873)
-- Name: view_inproceedings; Type: VIEW; Schema: slr; Owner: postgres
--

CREATE VIEW slr.view_inproceedings AS
 SELECT p.id,
    p.key_dblp,
    p.authors,
    p.doc_type,
    p.editor,
    p.pages,
    p.year,
    p.title,
    p.address,
    p.journal,
    p.volume,
    p.number,
    p.month,
    p.url,
    p.ee,
    p.cdrom,
    p.cite,
    p.publisher,
    p.note,
    p.crossref,
    p.isbn,
    p.series,
    p.school,
    p.chapter,
    p.publnr,
    p.book_title,
    p.mdate,
    p.reg_date,
    p.updated_state,
    p.grupo
   FROM slr.dblp_publication p
  WHERE ((p.doc_type)::text = 'inproceedings'::text);


ALTER TABLE slr.view_inproceedings OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 23867)
-- Name: view_proceedings; Type: VIEW; Schema: slr; Owner: postgres
--

CREATE VIEW slr.view_proceedings AS
 SELECT p.id,
    p.key_dblp,
    p.authors,
    p.doc_type,
    p.editor,
    p.pages,
    p.year,
    p.title,
    p.address,
    p.journal,
    p.volume,
    p.number,
    p.month,
    p.url,
    p.ee,
    p.cdrom,
    p.cite,
    p.publisher,
    p.note,
    p.crossref,
    p.isbn,
    p.series,
    p.school,
    p.chapter,
    p.publnr,
    p.book_title,
    p.mdate,
    p.reg_date,
    p.updated_state,
    p.grupo
   FROM slr.dblp_publication p
  WHERE ((p.doc_type)::text = 'proceedings'::text);


ALTER TABLE slr.view_proceedings OWNER TO postgres;

--
-- TOC entry 3265 (class 2604 OID 23890)
-- Name: books id; Type: DEFAULT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.books ALTER COLUMN id SET DEFAULT nextval('slr.books_id_seq'::regclass);


--
-- TOC entry 3289 (class 2604 OID 24053)
-- Name: conference_editorials id; Type: DEFAULT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_editorials ALTER COLUMN id SET DEFAULT nextval('slr.conference_editorials_id_seq'::regclass);


--
-- TOC entry 3287 (class 2604 OID 24043)
-- Name: conference_papers id; Type: DEFAULT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_papers ALTER COLUMN id SET DEFAULT nextval('slr.conference_papers_id_seq'::regclass);


--
-- TOC entry 3274 (class 2604 OID 23947)
-- Name: editions id; Type: DEFAULT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.editions ALTER COLUMN id SET DEFAULT nextval('slr.editions_id_seq'::regclass);


--
-- TOC entry 3283 (class 2604 OID 24028)
-- Name: journal_editorials id; Type: DEFAULT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_editorials ALTER COLUMN id SET DEFAULT nextval('slr.journal_editorial_id_seq'::regclass);


--
-- TOC entry 3285 (class 2604 OID 24036)
-- Name: journal_papers id; Type: DEFAULT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_papers ALTER COLUMN id SET DEFAULT nextval('slr.journal_papers_id_seq'::regclass);


--
-- TOC entry 3261 (class 2604 OID 23791)
-- Name: publications id; Type: DEFAULT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.publications ALTER COLUMN id SET DEFAULT nextval('slr.publications_id_seq'::regclass);


--
-- TOC entry 3312 (class 2606 OID 23836)
-- Name: author_publications author_publications_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.author_publications
    ADD CONSTRAINT author_publications_pkey PRIMARY KEY (id);


--
-- TOC entry 3344 (class 2606 OID 50685)
-- Name: authors authors__pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.authors
    ADD CONSTRAINT authors__pkey PRIMARY KEY (id);


--
-- TOC entry 3308 (class 2606 OID 23712)
-- Name: authors_ant authors_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.authors_ant
    ADD CONSTRAINT authors_pkey PRIMARY KEY (id);


--
-- TOC entry 3314 (class 2606 OID 23892)
-- Name: books books_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.books
    ADD CONSTRAINT books_pkey PRIMARY KEY (id);


--
-- TOC entry 3340 (class 2606 OID 24055)
-- Name: conference_editorials conference_editorials_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_editorials
    ADD CONSTRAINT conference_editorials_pkey PRIMARY KEY (id);


--
-- TOC entry 3338 (class 2606 OID 24045)
-- Name: conference_papers conference_papers_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_papers
    ADD CONSTRAINT conference_papers_pkey PRIMARY KEY (id);


--
-- TOC entry 3330 (class 2606 OID 23992)
-- Name: conferences conferences_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conferences
    ADD CONSTRAINT conferences_pkey PRIMARY KEY (id);


--
-- TOC entry 3302 (class 2606 OID 23480)
-- Name: countries countries_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.countries
    ADD CONSTRAINT countries_pkey PRIMARY KEY (id);


--
-- TOC entry 3298 (class 2606 OID 23201)
-- Name: dblp_publication dblp_publication_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.dblp_publication
    ADD CONSTRAINT dblp_publication_pkey PRIMARY KEY (id);


--
-- TOC entry 3342 (class 2606 OID 50620)
-- Name: dblp_publications dblp_publications_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.dblp_publications
    ADD CONSTRAINT dblp_publications_pkey PRIMARY KEY (id);


--
-- TOC entry 3306 (class 2606 OID 23694)
-- Name: departments departments_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.departments
    ADD CONSTRAINT departments_pkey PRIMARY KEY (id);


--
-- TOC entry 3321 (class 2606 OID 23949)
-- Name: editions editions_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.editions
    ADD CONSTRAINT editions_pkey PRIMARY KEY (id);


--
-- TOC entry 3319 (class 2606 OID 23923)
-- Name: book_chapters incollections_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.book_chapters
    ADD CONSTRAINT incollections_pkey PRIMARY KEY (id);


--
-- TOC entry 3304 (class 2606 OID 23637)
-- Name: institutions institutions_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.institutions
    ADD CONSTRAINT institutions_pkey PRIMARY KEY (id);


--
-- TOC entry 3334 (class 2606 OID 24030)
-- Name: journal_editorials journal_editorial_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_editorials
    ADD CONSTRAINT journal_editorial_pkey PRIMARY KEY (id);


--
-- TOC entry 3336 (class 2606 OID 24047)
-- Name: journal_papers journal_papers_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_papers
    ADD CONSTRAINT journal_papers_pkey PRIMARY KEY (id);


--
-- TOC entry 3332 (class 2606 OID 24004)
-- Name: journals journals_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journals
    ADD CONSTRAINT journals_pkey PRIMARY KEY (id);


--
-- TOC entry 3326 (class 2606 OID 23961)
-- Name: keywords keywords_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.keywords
    ADD CONSTRAINT keywords_pkey PRIMARY KEY (id);


--
-- TOC entry 3328 (class 2606 OID 23970)
-- Name: publication_keywords publication_keywords_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.publication_keywords
    ADD CONSTRAINT publication_keywords_pkey PRIMARY KEY (id);


--
-- TOC entry 3310 (class 2606 OID 23796)
-- Name: publications publications_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.publications
    ADD CONSTRAINT publications_pkey PRIMARY KEY (id);


--
-- TOC entry 3317 (class 2606 OID 23910)
-- Name: publishers publishers_pkey; Type: CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.publishers
    ADD CONSTRAINT publishers_pkey PRIMARY KEY (id);


--
-- TOC entry 3345 (class 1259 OID 50691)
-- Name: idx_authors__name; Type: INDEX; Schema: slr; Owner: postgres
--

CREATE INDEX idx_authors__name ON slr.authors USING btree (names COLLATE "C" text_pattern_ops);


--
-- TOC entry 3299 (class 1259 OID 25151)
-- Name: idx_dblppublication__crossref; Type: INDEX; Schema: slr; Owner: postgres
--

CREATE INDEX idx_dblppublication__crossref ON slr.dblp_publication USING btree (crossref COLLATE "C" varchar_pattern_ops);


--
-- TOC entry 3300 (class 1259 OID 25150)
-- Name: idx_dblppublication__keydblp; Type: INDEX; Schema: slr; Owner: postgres
--

CREATE INDEX idx_dblppublication__keydblp ON slr.dblp_publication USING btree (key_dblp COLLATE "C" varchar_pattern_ops);


--
-- TOC entry 3322 (class 1259 OID 25786)
-- Name: idx_editions__description; Type: INDEX; Schema: slr; Owner: postgres
--

CREATE INDEX idx_editions__description ON slr.editions USING btree (description COLLATE "C" varchar_ops);


--
-- TOC entry 3323 (class 1259 OID 25785)
-- Name: idx_editions__number; Type: INDEX; Schema: slr; Owner: postgres
--

CREATE INDEX idx_editions__number ON slr.editions USING btree (number COLLATE "C" varchar_ops);


--
-- TOC entry 3324 (class 1259 OID 25144)
-- Name: idx_editions__volume; Type: INDEX; Schema: slr; Owner: postgres
--

CREATE INDEX idx_editions__volume ON slr.editions USING btree (volume COLLATE "C" varchar_ops);


--
-- TOC entry 3513 (class 0 OID 0)
-- Dependencies: 3324
-- Name: INDEX idx_editions__volume; Type: COMMENT; Schema: slr; Owner: postgres
--

COMMENT ON INDEX slr.idx_editions__volume IS 'index volume field in slr.editions';


--
-- TOC entry 3315 (class 1259 OID 24076)
-- Name: idx_publishers__description; Type: INDEX; Schema: slr; Owner: postgres
--

CREATE INDEX idx_publishers__description ON slr.publishers USING btree (description COLLATE "C" text_pattern_ops);


--
-- TOC entry 3367 (class 2606 OID 50686)
-- Name: authors fk_author__department; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.authors
    ADD CONSTRAINT fk_author__department FOREIGN KEY (department_id) REFERENCES slr.departments(id);


--
-- TOC entry 3348 (class 2606 OID 23837)
-- Name: author_publications fk_author_publication__author; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.author_publications
    ADD CONSTRAINT fk_author_publication__author FOREIGN KEY (author_id) REFERENCES slr.authors_ant(id);


--
-- TOC entry 3349 (class 2606 OID 23842)
-- Name: author_publications fk_author_publication__publication; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.author_publications
    ADD CONSTRAINT fk_author_publication__publication FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3355 (class 2606 OID 24013)
-- Name: editions fk_conference_id__id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.editions
    ADD CONSTRAINT fk_conference_id__id FOREIGN KEY (conference_id) REFERENCES slr.conferences(id);


--
-- TOC entry 3347 (class 2606 OID 23695)
-- Name: departments fk_department__institution; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.departments
    ADD CONSTRAINT fk_department__institution FOREIGN KEY (institution_id) REFERENCES slr.institutions(id);


--
-- TOC entry 3364 (class 2606 OID 24082)
-- Name: conference_papers fk_edition_id__conference_papers; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_papers
    ADD CONSTRAINT fk_edition_id__conference_papers FOREIGN KEY (edition_id) REFERENCES slr.editions(id);


--
-- TOC entry 3365 (class 2606 OID 24077)
-- Name: conference_editorials fk_edition_id__editions_id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_editorials
    ADD CONSTRAINT fk_edition_id__editions_id FOREIGN KEY (edition_id) REFERENCES slr.editions(id);


--
-- TOC entry 3360 (class 2606 OID 24087)
-- Name: journal_editorials fk_edition_id__journal_editorials; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_editorials
    ADD CONSTRAINT fk_edition_id__journal_editorials FOREIGN KEY (edition_id) REFERENCES slr.editions(id);


--
-- TOC entry 3362 (class 2606 OID 24092)
-- Name: journal_papers fk_edition_id__journal_papers_id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_papers
    ADD CONSTRAINT fk_edition_id__journal_papers_id FOREIGN KEY (edition_id) REFERENCES slr.editions(id);


--
-- TOC entry 3346 (class 2606 OID 23638)
-- Name: institutions fk_institution__country; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.institutions
    ADD CONSTRAINT fk_institution__country FOREIGN KEY (country_id) REFERENCES slr.countries(id);


--
-- TOC entry 3356 (class 2606 OID 24018)
-- Name: editions fk_journal_id__id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.editions
    ADD CONSTRAINT fk_journal_id__id FOREIGN KEY (journal_id) REFERENCES slr.journals(id);


--
-- TOC entry 3357 (class 2606 OID 23971)
-- Name: publication_keywords fk_publcation_keywords__keyword; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.publication_keywords
    ADD CONSTRAINT fk_publcation_keywords__keyword FOREIGN KEY (keyword_id) REFERENCES slr.keywords(id);


--
-- TOC entry 3366 (class 2606 OID 24056)
-- Name: conference_editorials fk_publication_id__conference_editorials; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_editorials
    ADD CONSTRAINT fk_publication_id__conference_editorials FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3363 (class 2606 OID 24061)
-- Name: conference_papers fk_publication_id__conference_papers_id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.conference_papers
    ADD CONSTRAINT fk_publication_id__conference_papers_id FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3350 (class 2606 OID 23893)
-- Name: books fk_publication_id__id_books; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.books
    ADD CONSTRAINT fk_publication_id__id_books FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3352 (class 2606 OID 23924)
-- Name: book_chapters fk_publication_id__id_collections; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.book_chapters
    ADD CONSTRAINT fk_publication_id__id_collections FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3359 (class 2606 OID 24066)
-- Name: journal_editorials fk_publication_id__journal_editorials_id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_editorials
    ADD CONSTRAINT fk_publication_id__journal_editorials_id FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3361 (class 2606 OID 24071)
-- Name: journal_papers fk_publication_id__journal_papers_id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.journal_papers
    ADD CONSTRAINT fk_publication_id__journal_papers_id FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3358 (class 2606 OID 23976)
-- Name: publication_keywords fk_publication_keywords__publication; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.publication_keywords
    ADD CONSTRAINT fk_publication_keywords__publication FOREIGN KEY (publication_id) REFERENCES slr.publications(id);


--
-- TOC entry 3354 (class 2606 OID 24008)
-- Name: editions fk_publisher_id__id; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.editions
    ADD CONSTRAINT fk_publisher_id__id FOREIGN KEY (publisher_id) REFERENCES slr.publishers(id);


--
-- TOC entry 3353 (class 2606 OID 23929)
-- Name: book_chapters fk_publisher_id__id__collections; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.book_chapters
    ADD CONSTRAINT fk_publisher_id__id__collections FOREIGN KEY (publisher_id) REFERENCES slr.publishers(id);


--
-- TOC entry 3351 (class 2606 OID 23911)
-- Name: books fk_publisher_id__id_books; Type: FK CONSTRAINT; Schema: slr; Owner: postgres
--

ALTER TABLE ONLY slr.books
    ADD CONSTRAINT fk_publisher_id__id_books FOREIGN KEY (publisher_id) REFERENCES slr.publishers(id);


-- Completed on 2021-03-28 11:31:52 EDT

--
-- PostgreSQL database dump complete
--

