# IDEA Classification of Instruction Program (CIP) Management Command Line Interface (CLI)
Manage IDEA Classification of Instruction Program (CIP) data.

The Main class provides a way to insert new Classification of Instructional Programs (CIPs) into
the Combo database. This is done through the use of a CSV file where each row is a CIP which includes
the code, name/title, definition, year, cross-references, and the association with older IDEA disciplines.

The arguments include:
- v (verbose) - provide more output on the command line
- t (trialRun) - perform a trial run of the reading and insertion
- d (database) - define the name of the database to update.
- h (host) - the given server hosts the database.
- p (port) - the given port hosts the database.
- u (user) - the user name to use when connecting to the database.
- pa (password) - the password to use when connecting to the database.
- f (file) - the given file (CSV) has the updates to make.
- ? (help) - show the usage of this