<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/helpers.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    json_response(['ok' => false, 'message' => 'POST requests only.'], 405);
}

$data = json_input();
$firstName = required_string($data, 'firstName', 'First name');
$lastName = required_string($data, 'lastName', 'Last name');
$phone = required_string($data, 'phone', 'Phone number');
$street = required_string($data, 'street', 'Street address');
$city = required_string($data, 'city', 'City');
$state = strtoupper(required_string($data, 'state', 'State'));
$zip = required_string($data, 'zip', 'ZIP code');
$email = strtolower(required_string($data, 'email', 'Email address'));
$password = (string)($data['password'] ?? '');
$boatName = required_string($data, 'boatName', 'Boat name');
$boatLength = (float)($data['boatLength'] ?? 0);
$boatType = trim((string)($data['boatType'] ?? ''));
$registrationNumber = trim((string)($data['registrationNumber'] ?? ''));

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    json_response(['ok' => false, 'message' => 'Enter a valid email address.'], 422);
}
if (!preg_match('/^\d{5}(-\d{4})?$/', $zip)) {
    json_response(['ok' => false, 'message' => 'Enter a valid ZIP code.'], 422);
}
if (strlen($state) !== 2) {
    json_response(['ok' => false, 'message' => 'Use the two-letter state abbreviation.'], 422);
}
if ($boatLength <= 0 || $boatLength > 200) {
    json_response(['ok' => false, 'message' => 'Boat length must be between 1 and 200 feet.'], 422);
}
if (
    strlen($password) < 8 ||
    !preg_match('/[A-Z]/', $password) ||
    !preg_match('/[a-z]/', $password) ||
    !preg_match('/\d/', $password) ||
    !preg_match('/[^A-Za-z0-9]/', $password)
) {
    json_response(['ok' => false, 'message' => 'Password must be at least 8 characters and include uppercase, lowercase, a number, and a special character.'], 422);
}

$exists = $pdo->prepare('SELECT customer_id FROM customers WHERE email = ?');
$exists->execute([$email]);
if ($exists->fetch()) {
    json_response(['ok' => false, 'message' => 'An account already exists for this email address. Please sign in instead.'], 409);
}

try {
    $pdo->beginTransaction();
    $customerStmt = $pdo->prepare(
        'INSERT INTO customers (first_name, last_name, phone, street, city, state, zip, email, password_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)'
    );
    $customerStmt->execute([
        $firstName, $lastName, $phone, $street, $city, $state, $zip, $email,
        password_hash($password, PASSWORD_DEFAULT)
    ]);
    $customerId = (int)$pdo->lastInsertId();

    $boatStmt = $pdo->prepare(
        'INSERT INTO boats (customer_id, boat_name, boat_length_ft, boat_type, registration_number) VALUES (?, ?, ?, ?, ?)'
    );
    $boatStmt->execute([$customerId, $boatName, $boatLength, $boatType ?: null, $registrationNumber ?: null]);
    $boatId = (int)$pdo->lastInsertId();

    $pdo->commit();
    session_regenerate_id(true);
    $_SESSION['customer_id'] = $customerId;

    json_response([
        'ok' => true,
        'message' => 'Account and boat information saved.',
        'customerId' => $customerId,
        'boatId' => $boatId
    ], 201);
} catch (Throwable $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    json_response(['ok' => false, 'message' => 'The account could not be created.'], 500);
}
