/*
Alexander Baldree
Max Jankowski
Aftabur Rahman
Jordan Dardar

Green team Module 4 CSD-460

Build from ERD, used Jordans SQL script to build tables, defaults to Jordans SQL for ease. but can change to where differances where found going to the erd.
Noticed Also that the ERD had the 'notes TEXT' column missing, I think we should include this in the final format.  

This file creates a localized DB we will be working with, and seeds 5 client files for now. We will still need to add to this. We can also use this to look at if we need to chaneg scheme. 
WE will also need to add HASHING STILL, will sumit a suggestions over discord for bcrypt. 
In addition to this java file, I will add a db.properties file as from what I read is good practice. 

 */

package com.moffatbay.db;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap; // Dont get excited, not a hash for the password. But we'll get there 
import java.util.Map;
import java.util.Properties;

public class GreenTeamDataBaseIdea {

	// Seeding data with immutable data holders. this also is simpler thet using multiple arrays. 
    private record CustomerSeed(
            String firstName, String lastName, String phone,
            String street, String city, String state, String zip,
            String email, String password, boolean emailVerified
    ) {}

    private static final CustomerSeed[] CUSTOMER_SEEDS = {

        // Persona for Henry Morrison - retired fishing boat captain.        
        new CustomerSeed("Henry", "Morrison", "(360) 555-0110",
            "12 Trawler Way", "Westport", "WA", "98595",
            "henry.morrison@example.com", "CaptainH2026!", true),

        // Priya Sharma - new customer, wants to register
        // and reserve a slip in one sitting.
        new CustomerSeed("Priya", "Sharma", "(425) 555-0111",
            "88 Overlook Ave", "Bellevue", "WA", "98004",
            "priya.sharma@example.com", "PriyaPM2026!", true),

        // Emily Tran - returning customer, already has a
        // 40 ft slip reserved, wants fast reservation lookup.
        new CustomerSeed("Emily", "Tran", "(425) 555-0112",
            "215 Cedar Ridge Dr", "Bellevue", "WA", "98004",
            "emily.tran@example.com", "NurseEmily26!", true),

        // Simple new customer John Ruiz or as I call him JR 
        new CustomerSeed("John", "Ruiz", "(360) 555-0113",
            "40 Dockside Ln", "Moffat Bay", "WA", "98599",
            "john.ruiz@example.com", "JohnSail26!", true),
      
		// Fans of the turtles will know my friend Casey 
        new CustomerSeed("Casey", "Jones", "(360) 555-0114",
            "77 Tideway Ct", "Moffat Bay", "WA", "98599",
            "casey.jones@example.com", "CaseyTide26!", false)
    };

    private record BoatSeed(
            String ownerEmail, String boatName, double lengthFt,
            String boatType, String regNumber
    ) {}
 
	// the ownerEmail links a boat back to the Customer seed, we will look at this later 
    private static final BoatSeed[] BOAT_SEEDS = {
        new BoatSeed("henry.morrison@example.com", "Reel Adventure", 48.0, "Trawler", "WA-MB-4801"),
        new BoatSeed("priya.sharma@example.com", "Bellevue Breeze", 26.0, "Sailboat", "WA-MB-2602"),
        new BoatSeed("emily.tran@example.com", "Night Shift", 36.0, "Power Boat", "WA-MB-3603"),
        new BoatSeed("john.ruiz@example.com", "Second Wind", 34.0, "Cabin Cruiser", "WA-MB-3404"),
        new BoatSeed("casey.jones@example.com", "Changing Tides", 45.0, "Sailboat", "WA-MB-4505")
    };

    private record ReservationSeed(
            String ownerEmail, String slipNumber, String checkInDate,
            String expectedTerm, double monthlyCost, String notes,
            String status, String cancelledAt
    ) {}

    
	// The canceledAt is null for still active, only the cancelled row has a timestamp 
    private static final ReservationSeed[] RESERVATION_SEEDS = {

        // Priya registered and reserved in one session (26 ft boat so, goes without saying 26 ft slip).
        new ReservationSeed("priya.sharma@example.com", "A8", "2026-09-01",
            "12 months", 270.00, "Registered and reserved in a single session.",
            "CONFIRMED", null),

        // Emily already had a slip reserved (36 ft boat with a 40 ft slip).
        new ReservationSeed("emily.tran@example.com", "B4", "2026-09-15",
            "6 months", 370.00, "Returning customer; slip was already reserved on a prior visit.",
            "CONFIRMED", null),

        // Johns reservation is still pending 
        new ReservationSeed("john.ruiz@example.com", "B5", "2026-10-01",
            "6 months", 350.00, "Awaiting payment confirmation before the slip is finalized.",
            "PENDING", null),

        // Casey: cancelled after a change of plans, all this to prove a concept 
        new ReservationSeed("casey.jones@example.com", "C1", "2026-10-15",
            "3 months", 460.00, "Customer cancelled after a change of plans.",
            "CANCELLED", "2026-08-24 15:00:00")
    };

    private record WaitlistSeed(String ownerEmail, int slipSize, String status) {}

    private static final WaitlistSeed[] WAITLIST_SEEDS = {
        // Henry: checked 50 ft availability, none open, joined the wait list.
        new WaitlistSeed("henry.morrison@example.com", 50, "WAITING"),
        // Casey: cancelled her reservation, then joined the wait list instead.
        new WaitlistSeed("casey.jones@example.com", 50, "CONTACTED")
    };

    // the slip information will match the visual map in the future, but thats down the road and lets not dwell on it now. 
    private static final Object[][] SLIP_SEEDS = {
        {26, "A8", "RESERVED"},
        {26, "A9", "AVAILABLE"},
        {40, "B4", "RESERVED"},
        {40, "B5", "HELD"},
        {50, "C1", "AVAILABLE"},
        {50, "C2", "AVAILABLE"}
    };
	
	// The entry point finally, reads the setting for connection. makes sue the DB exists and makes the tables. Then seeds data 
       public static void main(String[] args) {
        Properties props = loadProperties();
 
        String host = props.getProperty("db.host", "localhost");
        String port = props.getProperty("db.port", "3306");
        String dbName = props.getProperty("db.name", "moffat_bay");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");
 
        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/";
        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
 
        try {
            // the database itself alread need to be there before we connect to it.          
            ensureDatabaseExists(serverUrl, user, password, dbName);
 
            
			// now connect to our moffat DB for everything else. 
            try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
                System.out.println("Connected to database: " + dbName);
 
                createTables(conn);
 
                if (isAlreadySeeded(conn)) {
                    System.out.println("customers table already has data - skipping seed step.");
                } else {
                    printPasswordWarning();
                    seedDemoData(conn);
                    System.out.println("Seed data inserted: 5 customers "
                        + "(3 persona-based, 2 generic), plus boats, slip_types, "
                        + "slips, reservations, waitlist_entries, and email_verifications.");
                }
 
                printSummary(conn);
            }
 
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
  
	 // Laoding the db.properties from path. this is to make credentials are outside the source. I just dont want us to be dinged for something simple like that. 
    private static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream in = GreenTeamDataBaseIdea.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (in == null) {
                throw new IOException(
                    "db.properties not found on the classpath. "
                    + "Copy the template into your resources folder first.");
            }
            props.load(in);

        } catch (IOException e) {
            throw new RuntimeException("Could not load db.properties", e);
        }
        return props;
    }
    
	 // makes the DB itself IF it isnt there.  the JDBC URL indicates the DB so we first connect to server root, then CREATE. then another connection 
	 //opens and we'll target that DB 
    private static void ensureDatabaseExists(
            String serverUrl, String user, String password, String dbName) throws SQLException {

        try (Connection conn = DriverManager.getConnection(serverUrl, user, password);
             Statement st = conn.createStatement()) {

            st.executeUpdate(
                "CREATE DATABASE IF NOT EXISTS " + dbName
                + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }


	// Now for more funn parts, making those tables. I will be using the CREATE TABLE IF NOT EXISTS here so we dont get those duplicate tables when running multiple times 
	// Used alot of copy paste here with Jordans work, So thanks Jordan 
    private static void createTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {

            // the parent table every other table eventually traces back here          
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS customers ("
                + "customer_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
                + "first_name VARCHAR(80) NOT NULL,"
                + "last_name VARCHAR(80) NOT NULL,"
                + "phone VARCHAR(30) NOT NULL,"
                + "street VARCHAR(120) NOT NULL,"
                + "city VARCHAR(80) NOT NULL,"
                + "state CHAR(2) NOT NULL,"
                + "zip VARCHAR(10) NOT NULL,"
                + "email VARCHAR(190) NOT NULL UNIQUE,"   // agian, dead horse, bet no hashing yet, storing in plain test                
                + "password_hash VARCHAR(255) NOT NULL,"
                + "email_verified BOOLEAN NOT NULL DEFAULT FALSE,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB");

            // our 3 project slip types 26/40/50 footers          
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS slip_types ("
                + "slip_type_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
                + "size_ft INT NOT NULL,"
                + "total_capacity INT NOT NULL,"
                + "rate_per_foot DECIMAL(8,2) NOT NULL,"
                + "electric_fee DECIMAL(8,2) NOT NULL"
                + ") ENGINE=InnoDB");
         
			// Every boat will belong to 1 client, but a customer can have multiple boats. 
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS boats ("
                + "boat_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
                + "customer_id INT UNSIGNED NOT NULL,"
                + "boat_name VARCHAR(100) NOT NULL,"
                + "boat_length_ft DECIMAL(6,1) NOT NULL,"
                + "boat_type VARCHAR(60),"
                + "registration_number VARCHAR(80),"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "CONSTRAINT fk_boats_customer FOREIGN KEY (customer_id) "
                + "REFERENCES customers(customer_id)"
                + ") ENGINE=InnoDB");
            
			// all slips belong to one slip_type, were putting status as a plain VARCHAR for now. But new status can be added without alter table 
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS slips ("
                + "slip_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
                + "slip_type_id INT UNSIGNED NOT NULL,"
                + "slip_number VARCHAR(20) NOT NULL UNIQUE,"
                + "status VARCHAR(30) NOT NULL,"
                + "CONSTRAINT fk_slips_slip_type FOREIGN KEY (slip_type_id) "
                + "REFERENCES slip_types(slip_type_id)"
                + ") ENGINE=InnoDB");

            
			// Linking a client and boat to a slip, should match Aftab's ERD, this appeared to be missing from the SQL file sent by Jordan, Let me know if I missed anthing 
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS reservations ("
                + "reservation_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
                + "customer_id INT UNSIGNED NOT NULL,"
                + "boat_id INT UNSIGNED NOT NULL,"
                + "slip_id INT UNSIGNED NOT NULL,"
                + "check_in_date DATE NOT NULL,"
                + "expected_term VARCHAR(30) NOT NULL,"
                + "monthly_cost DECIMAL(10,2) NOT NULL,"
                + "notes TEXT,"
                + "status VARCHAR(30) NOT NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "cancelled_at TIMESTAMP NULL,"
                + "CONSTRAINT fk_reservations_customer FOREIGN KEY (customer_id) "
                + "REFERENCES customers(customer_id),"
                + "CONSTRAINT fk_reservations_boat FOREIGN KEY (boat_id) "
                + "REFERENCES boats(boat_id),"
                + "CONSTRAINT fk_reservations_slip FOREIGN KEY (slip_id) "
                + "REFERENCES slips(slip_id)"
                + ") ENGINE=InnoDB");


			// recording the client boat requested slip category when noting is there. Position on the list is calculated later from the joined_at
			// instead of be printed as a fixed value 
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS waitlist_entries ("
                + "waitlist_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
                + "customer_id INT UNSIGNED NOT NULL,"
                + "boat_id INT UNSIGNED NOT NULL,"
                + "slip_type_id INT UNSIGNED NOT NULL,"
                + "joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "status VARCHAR(30) NOT NULL,"
                + "CONSTRAINT fk_waitlist_customer FOREIGN KEY (customer_id) "
                + "REFERENCES customers(customer_id),"
                + "CONSTRAINT fk_waitlist_boat FOREIGN KEY (boat_id) "
                + "REFERENCES boats(boat_id),"
                + "CONSTRAINT fk_waitlist_slip_type FOREIGN KEY (slip_type_id) "
                + "REFERENCES slip_types(slip_type_id)"
                + ") ENGINE=InnoDB");


			// This one will be a bit of a hard time had a bit of aid to make sure it worked. 
			// One thing of note is that we dont have a hasing strategy yet, SO AGAIN it plain text for now. I have some ideas
			// One is we can do a SHA token for the email verification, but will need to do some research for that. And we can have a seperate hashing for login passwords 			
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS email_verifications ("
                + "verification_id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,"
                + "customer_id INT UNSIGNED NOT NULL,"
                + "token_hash VARCHAR(255) NOT NULL,"
                + "expires_at TIMESTAMP NOT NULL,"
                + "verified_at TIMESTAMP NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "CONSTRAINT fk_email_verifications_customer FOREIGN KEY (customer_id) "
                + "REFERENCES customers(customer_id)"
                + ") ENGINE=InnoDB");
        }

        System.out.println("Tables verified/created.");
    }

    
	 
	 // the isAlreadyseeded, was a suggustion that was provided by Gemini AI when using it to check for some errors I had. in addition I also has is provide me with two methods at the end of the code
	 // see insertEmailVerifications and printSummary. 
	 // The purpose of this method is to prevent duplication of the seedDemoData by assuming its already been seeded. SO AGIAN THIS IS GEMINI build full disclosure 
    private static boolean isAlreadySeeded(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM customers")) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }

 
	 
	 /*
	 ============================================================
	 
	 There is no hashing yet. Sorry but i will sound redundent about this for now, Just want to make notes were attention will be required in the future 
	 
	 ============================================================
	 */
    private static void printPasswordWarning() {
        System.out.println();
        System.out.println("*** WARNING: passwords are being stored in plain text. ***");
        System.out.println("*** This build has hashing turned off for the proof of concept. ***");
        System.out.println("*** Do not reuse these demo passwords anywhere real. ***");
        System.out.println();
    }

   
	// Now to insert seed info. Needed a refreasher here, used: https://www.geeksforgeeks.org/java/inserting-single-and-multiple-records-in-mysql-in-java/
	// Had some trouble bu found the fallback of conn.rollback to unto any issue if an inserts does go. it will undo what was done already. https://www.geeksforgeeks.org/java/java-program-to-make-a-rollback/
    private static void seedDemoData(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Each insert method below returns a Map so later steps can look up ID row with something we can read and see.            
            Map<Integer, Integer> slipTypeIdsBySize = insertSlipTypes(conn);
            Map<String, Integer> slipIdsByNumber = insertSlips(conn, slipTypeIdsBySize);
            Map<String, Integer> customerIdsByEmail = insertCustomers(conn);
            Map<String, Integer> boatIdsByOwnerEmail = insertBoats(conn, customerIdsByEmail);

            insertReservations(conn, customerIdsByEmail, boatIdsByOwnerEmail, slipIdsByNumber);
            insertWaitlistEntries(conn, customerIdsByEmail, boatIdsByOwnerEmail, slipTypeIdsBySize);
            insertEmailVerifications(conn, customerIdsByEmail);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
   
	 // insering 3 slip categories and returning lookup.
    private static Map<Integer, Integer> insertSlipTypes(Connection conn) throws SQLException {
        Map<Integer, Integer> slipTypeIdsBySize = new HashMap<>();

        String sql = "INSERT INTO slip_types (size_ft, total_capacity, rate_per_foot, electric_fee) "
            + "VALUES (?, ?, ?, ?)";

        //  slip size in feet, total number of slips of that size 
        int[][] data = {
            {26, 30},
            {40, 24},
            {50, 18}
        };

        // Statement.RETURN_GENERATED_KEYS hands back the auto increment each insert made 
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int[] row : data) {
                ps.setInt(1, row[0]);
                ps.setInt(2, row[1]);
                ps.setBigDecimal(3, new BigDecimal("10.00"));
                ps.setBigDecimal(4, new BigDecimal("10.00"));
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        slipTypeIdsBySize.put(row[0], keys.getInt(1));
                    }
                }
            }
        }

        return slipTypeIdsBySize;
    }
    
	// making for now 6 demo slips and returning a slip number like a8 so that reservations can attach it to the correct slip  
    private static Map<String, Integer> insertSlips(
            Connection conn, Map<Integer, Integer> slipTypeIdsBySize) throws SQLException {

        Map<String, Integer> slipIdsByNumber = new HashMap<>();

        String sql = "INSERT INTO slips (slip_type_id, slip_number, status) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Object[] row : SLIP_SEEDS) {
                int size = (int) row[0];
                String slipNumber = (String) row[1];
                String status = (String) row[2];

                ps.setInt(1, slipTypeIdsBySize.get(size));
                ps.setString(2, slipNumber);
                ps.setString(3, status);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        slipIdsByNumber.put(slipNumber, keys.getInt(1));
                    }
                }
            }
        }

        return slipIdsByNumber;
    }
 
	 // inserts for now the 5 demo customers. No hashing yet, but will still use a question mark as a placeholder. Namely that becasue this is a prove of concept. May still add hashing by end of week. 
    private static Map<String, Integer> insertCustomers(Connection conn) throws SQLException {
        Map<String, Integer> customerIdsByEmail = new HashMap<>();

        String sql = "INSERT INTO customers "
            + "(first_name, last_name, phone, street, city, state, zip, "
            + "email, password_hash, email_verified) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (CustomerSeed c : CUSTOMER_SEEDS) {
                ps.setString(1, c.firstName());
                ps.setString(2, c.lastName());
                ps.setString(3, c.phone());
                ps.setString(4, c.street());
                ps.setString(5, c.city());
                ps.setString(6, c.state());
                ps.setString(7, c.zip());
                ps.setString(8, c.email());

                // Plain text for now we still need to talk about the hashing 
                ps.setString(9, c.password());

                ps.setBoolean(10, c.emailVerified());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        customerIdsByEmail.put(c.email(), keys.getInt(1));
                    }
                }
            }
        }

        System.out.println("Demo login credentials (for local testing only - plain text, see warning above):");
        for (CustomerSeed c : CUSTOMER_SEEDS) {
            System.out.println("  " + c.email() + " / " + c.password());
        }

        return customerIdsByEmail;
    }


	// inserts one boat per cleint and gives lookup from the owners email to make a boat ID so reservation point to the correct boat
    private static Map<String, Integer> insertBoats(
            Connection conn, Map<String, Integer> customerIdsByEmail) throws SQLException {

        Map<String, Integer> boatIdsByOwnerEmail = new HashMap<>();

        String sql = "INSERT INTO boats "
            + "(customer_id, boat_name, boat_length_ft, boat_type, registration_number) "
            + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (BoatSeed b : BOAT_SEEDS) {
                // Every BOAT_SEEDS entry's ownerEmail must exist in CUSTOMER_SEEDS above, or this lookup returns null
                // and the next line throws a NullPointerException. This is going to come in later when we need to seed this with more clients on 
				// a live and non-local server. 
                Integer customerId = customerIdsByEmail.get(b.ownerEmail());

                ps.setInt(1, customerId);
                ps.setString(2, b.boatName());
                ps.setBigDecimal(3, BigDecimal.valueOf(b.lengthFt()));
                ps.setString(4, b.boatType());
                ps.setString(5, b.regNumber());
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        boatIdsByOwnerEmail.put(b.ownerEmail(), keys.getInt(1));
                    }
                }
            }
        }

        return boatIdsByOwnerEmail;
    }
    
	// makes a reservation per reservations.seed row. only 4 of our 5 cleints now have reservations. since in henrt's case he wants to see te waitlist.  
    private static void insertReservations(
            Connection conn,
            Map<String, Integer> customerIdsByEmail,
            Map<String, Integer> boatIdsByOwnerEmail,
            Map<String, Integer> slipIdsByNumber) throws SQLException {

        String sql = "INSERT INTO reservations "
            + "(customer_id, boat_id, slip_id, check_in_date, expected_term, "
            + "monthly_cost, notes, status, cancelled_at) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ReservationSeed r : RESERVATION_SEEDS) {
                ps.setInt(1, customerIdsByEmail.get(r.ownerEmail()));
                ps.setInt(2, boatIdsByOwnerEmail.get(r.ownerEmail()));
                ps.setInt(3, slipIdsByNumber.get(r.slipNumber()));
                ps.setDate(4, Date.valueOf(r.checkInDate()));
                ps.setString(5, r.expectedTerm());
                ps.setBigDecimal(6, BigDecimal.valueOf(r.monthlyCost()));
                ps.setString(7, r.notes());
                ps.setString(8, r.status());

                // cancelled_at is nullable - only Casey's row has a
                // real value; everyone else gets an explicit SQL
                // NULL via ps.setNull(...) rather than leaving the
                // parameter unset (which JDBC does not allow).
                if (r.cancelledAt() != null) {
                    ps.setTimestamp(9, Timestamp.valueOf(r.cancelledAt()));
                } else {
                    ps.setNull(9, Types.TIMESTAMP);
                }

                ps.executeUpdate();
            }
        }
    }
	 
	 //Inserts two wait list rows, one per Seeds entry
    private static void insertWaitlistEntries(
            Connection conn,
            Map<String, Integer> customerIdsByEmail,
            Map<String, Integer> boatIdsByOwnerEmail,
            Map<Integer, Integer> slipTypeIdsBySize) throws SQLException {

        String sql = "INSERT INTO waitlist_entries "
            + "(customer_id, boat_id, slip_type_id, status) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (WaitlistSeed w : WAITLIST_SEEDS) {
                ps.setInt(1, customerIdsByEmail.get(w.ownerEmail()));
                ps.setInt(2, boatIdsByOwnerEmail.get(w.ownerEmail()));
                ps.setInt(3, slipTypeIdsBySize.get(w.slipSize()));
                ps.setString(4, w.status());
                ps.executeUpdate();
            }
        }
    }


		
    
	// Gemini helped with this one. will still need to look at the hashs, in this case for the verification tokens. So there is still alot of work to do. 
	// verified cleints will get verfied time stamps, in or case Caseys will verification will be false so that gives him SQL NULL so his account is still awaiting approval. 
        private static void insertEmailVerifications(
            Connection conn, Map<String, Integer> customerIdsByEmail) throws SQLException {
 
        String sql = "INSERT INTO email_verifications "
            + "(customer_id, token_hash, expires_at, verified_at) "
            + "VALUES (?, ?, ?, ?)";
 
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (CustomerSeed c : CUSTOMER_SEEDS) {
                Integer customerId = customerIdsByEmail.get(c.email());
                String token = "verify-" + c.firstName().toLowerCase() + "-demo-token";
 
                ps.setInt(1, customerId);
                ps.setString(2, token);
                ps.setTimestamp(3, Timestamp.valueOf("2026-09-05 09:00:00"));
 
                if (c.emailVerified()) {
                    ps.setTimestamp(4, Timestamp.valueOf("2026-08-22 10:00:00"));
                } else {
                    ps.setNull(4, Types.TIMESTAMP);
                }
 
                ps.executeUpdate();
            }
        }
    }


    // ------------------------------------------------------------
    // SUMMARY OUTPUT suggested by Gemini and Im not sold on it yet. Let me know. 
    //
    // Quick sanity check printed after every run - lets you
    // confirm at a glance that all 7 tables have the row counts
    // you expect, without opening phpMyAdmin.
    // ------------------------------------------------------------

    private static void printSummary(Connection conn) throws SQLException {
        String[] tables = {
            "customers", "slip_types", "boats", "slips",
            "reservations", "waitlist_entries", "email_verifications"
        };

        System.out.println("Row counts:");
        try (Statement st = conn.createStatement()) {
            for (String table : tables) {
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
                    rs.next();
                    System.out.println("  " + table + ": " + rs.getInt(1));
                }
            }
        }
    }
}
