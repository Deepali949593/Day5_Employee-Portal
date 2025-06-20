# 🗂️ Day 5 – Employee Management Portal (Java + MongoDB)

## 📌 Objective
Build a command-line Employee Management Portal using **Java** and **MongoDB** that supports:

- Adding, updating, and deleting employee records
- Filtering/searching employees
- Pagination and sorting
- Aggregation: Count employees per department

---

## ⚙️ Core Features

### ✅ 1. Add Employee
- Insert a document into the `employees` collection.
- Ensure the email is unique.

### ✅ 2. Update Employee
- Update specific fields (e.g., `department`, `skills`) without overwriting the whole document.

### ✅ 3. Delete Employee
- Delete using:
  - `email`, or
  - MongoDB `_id`

### ✅ 4. Search / Filter Employees
- By **Name** (partial match with regex)
- By **Department**
- By **Skill** (any match in the skills array)
- By **Joining Date Range** (e.g., 2023-01-01 to 2023-12-31)

### ✅ 5. List with Pagination
- Returns 5 records per page
- Sort by:
  - `name`
  - `joiningDate`

### ✅ 6. Department Statistics
- Aggregates total employees per department

```json
{ "_id": "IT", "count": 4 }
{ "_id": "HR", "count": 3 }
{ "_id": "Finance", "count": 3 }
