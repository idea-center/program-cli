package org.ideaedu.program

import au.com.bytecode.opencsv.CSVReader

import java.io.FileReader

import java.sql.DriverManager

/**
 * The Main class provides a way to insert new Classification of Instructional Programs (CIPs) into
 * the Combo database. This is done through the use of a CSV file where each row is a CIP which includes
 * the code, name/title, definition, year, cross-references, and the association with older IDEA disciplines.
 * The arguments include:
 * <ul>
 * <li>v (verbose) - provide more output on the command line</li>
 * <li>t (trialRun) - perform a trial run of the reading and insertion</li>
 * <li>d (database) - define the name of the database to update.</li>
 * <li>h (host) - the given server hosts the database.</li>
 * <li>p (port) - the given port hosts the database.</li>
 * <li>u (user) - the user name to use when connecting to the database.</li>
 * <li>pa (password) - the password to use when connecting to the database.</li>
 * <li>f (file) - the given file (CSV) has the updates to make.</li>
 * <li>? (help) - show the usage of this</li>
 * </ul>
 *
 * @author Todd Wallentine todd AT IDEAedu org
 */
public class Main {

    private static final def INSERT_PROGRAM_SQL = "INSERT INTO CIP (CIPYEAR, CODE, NAME, DEFINITION, CROSSREF, CREATED) VALUES (?, ?, ?, ?, ?, NOW())"
    private static final def INSERT_MAPPING_SQL = "INSERT INTO CIPTODISC (ID_DISCIPLINE, ID_CIP) VALUES ((SELECT ID_DISCIPLINE FROM DISCIPLINE WHERE CODE = ?), (SELECT ID_CIP FROM CIP WHERE CODE = ?))"

    private static final def DEFAULT_HOST = 'localhost'
    private static final def DEFAULT_PORT = 3306
    private static final def DEFAULT_DATABASE = 'combo'

    private static def host = DEFAULT_HOST
    private static def port = DEFAULT_PORT
    private static def database = DEFAULT_DATABASE
    private static def user
    private static def password
    private static def file

    private static def verboseOutput = false
    private static def trialRun = false

    public static void main(String[] args) {

        def cli = new CliBuilder( usage: 'Main -v -t -d database -h host -p port -u user -pa password -f csvFile' )
        cli.with {
            v longOpt: 'verbose', 'verbose output'
            t longOpt: 'trialRun', 'trial run of the reading and inserting'
            d longOpt: 'database', 'database name (default: combo)', args:1
            h longOpt: 'host', 'host name (default: localhost)', args:1
            p longOpt: 'port', 'port number (default: 3306)', args:1
            u longOpt: 'user', 'user name', args:1
            pa longOpt: 'password', 'password', args:1
            f longOpt: 'file', 'CSV file to load', args:1
            '?' longOpt: 'help', 'help'
        }
        def options = cli.parse(args)
        if(options.'?') {
            cli.usage()
            return
        }
        if(options.v) {
            verboseOutput = true
        }
        if(options.t) {
            trialRun = true
        }
        if(options.d) {
            database = options.d
        }
        if(options.h) {
            host = options.h
        }
        if(options.p) {
            port = options.p.toInteger()
        }
        if(options.u) {
            user = options.u
        }
        if(options.pa) {
            password = options.pa
        }
        if(options.f) {
            file = options.f
        }

        def f = new File(file)
        List<String[]> rows = new CSVReader(new FileReader(f)).readAll()

        def connection = DriverManager.getConnection("jdbc:mysql://${host}:${port}/${database}", user, password)
        def insertProgramStatement = connection.prepareStatement(INSERT_PROGRAM_SQL)
        def insertMappingStatement = connection.prepareStatement(INSERT_MAPPING_SQL)

        rows.remove(0) // Skip header row
        rows.each { row ->
            def disciplineCode = row[0].toInteger()
            def year = row[2].toInteger()
            def code = row[4]
            def title = row[5]
            def definition = row[6]
            def crossRef = row[7]

            if(verboseOutput) {
                println "Inserting a new Program - Code: ${code}, Year: ${year}, Title: ${title} and mapping it to ${disciplineCode}"
            }

            // Insert the new CIP
            insertProgramStatement.setInt(1, year)
            insertProgramStatement.setString(2, code)
            insertProgramStatement.setString(3, title)
            insertProgramStatement.setString(4, definition)
            insertProgramStatement.setString(5, crossRef)

            def insertProgramResult // 1 if the insert worked and 0 if it did not
            if(!trialRun) {
                insertProgramResult = insertProgramStatement.executeUpdate()
            } else {
                println "Executing SQL: ${INSERT_PROGRAM_SQL} with ${year}, '${code}', '${title}', '${definition}', '${crossRef}'"
                insertProgramResult = 1
            }
            insertProgramStatement.clearParameters()

            if(insertProgramResult) {
                // Insert the mapping of the CIP to DISCIPLINE into CIPTODISC
                insertMappingStatement.setInt(1, disciplineCode)
                insertMappingStatement.setString(2, code)

                def insertMappingResult // 1 if the insert worked and 0 if it did not
                if(!trialRun) {
                    insertMappingResult = insertMappingStatement.executeUpdate()
                } else {
                    println "Executing SQL: ${INSERT_MAPPING_SQL} with ${disciplineCode}, '${code}'"
                    insertMappingResult = 1
                }
                insertMappingStatement.clearParameters()

                if(!insertMappingResult) {
                    println "ERROR: CIP to DISCIPLINE mapping was not inserted (result: ${insertMappingResult})"
                }
            } else {
                println "ERROR: CIP was not inserted (result: ${insertProgramResult})"
            }
        }

        insertMappingStatement.close()
        insertProgramStatement.close()
    }
}