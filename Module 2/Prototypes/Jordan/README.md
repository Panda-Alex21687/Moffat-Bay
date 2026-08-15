# Moffat Bay Marina - Module 2 Prototype Contribution

**Student:** Jordan Dardar  
**Course:** CSD460 Capstone in Software Development  
**Team:** Green  
**Assigned user stories:** UST-04 and UST-06

## Files

- `index.html` - internal prototype hub for Jordan's assigned screens.
- `ust04-register.html` - registration/account creation prototype.
- `ust04-verification.html` - email verification workflow prototype.
- `ust04-login.html` - login and browser-only prototype session flow.
- `ust06-waitlist.html` - waitlist count, customers-ahead, and position-preview prototype.
- `styles.css` - shared visual design.
- `prototype.js` - front-end prototype interactions and validation.

## UST-04 coverage

GitHub issue #20 assigns these work items to UST-04: login/authentication workflow, email-format validation, email verification workflow, password security requirements, secure password storage, session management, and testing of authentication/verification scenarios.

The prototype demonstrates:

1. A registration form containing the Marina project's minimum required registration fields: email, first name, last name, telephone, boat name, boat length, and password.
2. Email used as the username.
3. Standard email-format validation.
4. The Marina project's stated password rule: at least 8 characters, at least one uppercase letter, and at least one lowercase letter.
5. A prototype-generated unique customer ID after successful registration.
6. A visual email verification step.
7. A login form using email and password.
8. A browser-only prototype session indicator after login.

**Prototype boundary:** Secure password hashing/storage, server-side authentication, actual email delivery, and production session management are intentionally not implemented in Module 2 because those are backend development functions.

## UST-06 coverage

GitHub issue #22 assigns these work items to UST-06: retrieve current waitlist count for a slip category, calculate/display number of customers ahead, display waitlist position before commitment, and test count/position scenarios.

The prototype demonstrates:

1. The project's three slip categories: 26 ft, 40 ft, and 50 ft.
2. The number of customers waiting for each category.
3. No customer names displayed on the waitlist screen.
4. A slip-category selector.
5. A preview of the number of customers ahead.
6. An estimated waitlist position shown before commitment.
7. A confirmation/cancel flow.
8. A prototype-only count change after confirmation so the interaction can be demonstrated.

**Prototype boundary:** Waitlist data is sample data. The production application will retrieve counts and calculate positions from MySQL.

## How to demonstrate

1. Open `index.html` in a browser.
2. Select **Start Registration Flow**.
3. Test invalid and valid email formats.
4. Test the password rules shown on screen.
5. Complete registration and use **Simulate Verification Link**.
6. Continue to **Login** and activate the prototype session.
7. Open **Wait List**.
8. Change slip categories to demonstrate the 26 ft, 40 ft, and 50 ft counts.
9. Select **Join Wait List** and show the customers-ahead and estimated-position confirmation before commitment.
10. Confirm or cancel. Use **Reset Prototype Sample Data** if another demonstration is needed.

## Accuracy notes

- The project requirements specify three marina slip sizes: **26 ft, 40 ft, and 50 ft**.
- The Marina registration requirements specify email, first name, last name, telephone, boat name, boat length, and password as minimum fields.
- The Marina password requirement states at least **8 characters**, including at least **one uppercase** and **one lowercase** letter.
- The waitlist lookup requirement says to display the number of customers waiting for each slip-size category and **not customer names**.
