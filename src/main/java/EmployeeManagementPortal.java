import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class EmployeeManagementPortal {
    private static final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private static final MongoDatabase database = mongoClient.getDatabase("company");
    private static final MongoCollection<Document> collection = database.getCollection("employees");

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- Employee Management Portal ---");
            System.out.println("1. Add Employee");
            System.out.println("2. Update Employee");
            System.out.println("3. Delete Employee");
            System.out.println("4. Search Employees");
            System.out.println("5. List with Pagination");
            System.out.println("6. Department Statistics");
            System.out.println("7. Exit");
            System.out.print("Choose option: ");
            int option = sc.nextInt(); sc.nextLine();

            switch (option) {
                case 1 -> addEmployee(sc);
                case 2 -> updateEmployee(sc);
                case 3 -> deleteEmployee(sc);
                case 4 -> searchEmployees(sc);
                case 5 -> paginateEmployees(sc);
                case 6 -> departmentStats();
                case 7 -> {
                    mongoClient.close();
                    return;
                }
                default -> System.out.println("Invalid Option!");
            }
        }
    }

    // 1. Add
    public static void addEmployee(Scanner sc) throws Exception {
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();

        if (collection.find(Filters.eq("email", email)).first() != null) {
            System.out.println("Email already exists!");
            return;
        }

        System.out.print("Enter department: ");
        String department = sc.nextLine();
        System.out.print("Enter joining date (yyyy-MM-dd): ");
        String joiningDateStr = sc.nextLine();
        Date joiningDate = new SimpleDateFormat("yyyy-MM-dd").parse(joiningDateStr);

        System.out.print("Enter skills (comma-separated): ");
        List<String> skills = Arrays.asList(sc.nextLine().split(","));

        Document employee = new Document("name", name)
                .append("email", email)
                .append("department", department)
                .append("joiningDate", joiningDate)
                .append("skills", skills);

        collection.insertOne(employee);
        System.out.println("Employee added.");
    }

    // 2. Update
    public static void updateEmployee(Scanner sc) {
        System.out.print("Enter email of employee to update: ");
        String email = sc.nextLine();

        Bson filter = Filters.eq("email", email);
        if (collection.find(filter).first() == null) {
            System.out.println("Employee not found.");
            return;
        }

        Document updateFields = new Document();
        System.out.print("Update department? (leave blank to skip): ");
        String department = sc.nextLine();
        if (!department.isEmpty()) updateFields.append("department", department);

        System.out.print("Update skills (comma-separated, blank to skip): ");
        String skillInput = sc.nextLine();
        if (!skillInput.isEmpty()) {
            List<String> skills = Arrays.asList(skillInput.split(","));
            updateFields.append("skills", skills);
        }

        if (updateFields.isEmpty()) {
            System.out.println("No fields to update.");
        } else {
            collection.updateOne(filter, new Document("$set", updateFields));
            System.out.println("Employee updated.");
        }
    }

    // 3. Delete
    public static void deleteEmployee(Scanner sc) {
        System.out.print("Delete by Email or ID? (email/id): ");
        String method = sc.nextLine();

        Bson filter;
        if (method.equalsIgnoreCase("email")) {
            System.out.print("Enter email: ");
            filter = Filters.eq("email", sc.nextLine());
        } else {
            System.out.print("Enter _id: ");
            filter = Filters.eq("_id", new org.bson.types.ObjectId(sc.nextLine()));
        }

        var result = collection.deleteOne(filter);
        System.out.println(result.getDeletedCount() + " record(s) deleted.");
    }

    // 4. Search
    public static void searchEmployees(Scanner sc) throws Exception {
        List<Bson> filters = new ArrayList<>();

        System.out.print("Search by name (partial)? ");
        String name = sc.nextLine();
        if (!name.isEmpty()) filters.add(Filters.regex("name", Pattern.compile(name, Pattern.CASE_INSENSITIVE)));

        System.out.print("Department? ");
        String dept = sc.nextLine();
        if (!dept.isEmpty()) filters.add(Filters.eq("department", dept));

        System.out.print("Skill? ");
        String skill = sc.nextLine();
        if (!skill.isEmpty()) filters.add(Filters.in("skills", skill));

        System.out.print("Joining date range (yyyy-MM-dd to yyyy-MM-dd)? ");
        String range = sc.nextLine();
        if (!range.isEmpty()) {
            String[] dates = range.split(" to ");
            Date from = new SimpleDateFormat("yyyy-MM-dd").parse(dates[0]);
            Date to = new SimpleDateFormat("yyyy-MM-dd").parse(dates[1]);
            filters.add(Filters.and(Filters.gte("joiningDate", from), Filters.lte("joiningDate", to)));
        }

        Bson query = filters.isEmpty() ? new Document() : Filters.and(filters);

        for (Document doc : collection.find(query)) {
            System.out.println(doc.toJson());
        }
    }

    // 5. Pagination
    public static void paginateEmployees(Scanner sc) {
        System.out.print("Page number: ");
        int page = sc.nextInt(); sc.nextLine();
        System.out.print("Sort by name or date? ");
        String sortField = sc.nextLine().equalsIgnoreCase("name") ? "name" : "joiningDate";

        int pageSize = 5;
        Bson sort = Sorts.ascending(sortField);

        collection.find()
                .sort(sort)
                .skip((page - 1) * pageSize)
                .limit(pageSize)
                .forEach(doc -> System.out.println(doc.toJson()));
    }

    // 6. Department stats
    public static void departmentStats() {
        List<Bson> pipeline = Arrays.asList(
                Aggregates.group("$department", Accumulators.sum("count", 1)),
                Aggregates.sort(Sorts.descending("count"))
        );

        collection.aggregate(pipeline).forEach(doc -> System.out.println(doc.toJson()));
    }
}
