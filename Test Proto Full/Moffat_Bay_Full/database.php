<!--
Alexander Baldree
Max Jankowski
Aftabur Rahman
Jordan Dardar

Moffat Bay Marina Project
The Green Team
CSD460
-->

<?php
// Moffat Bay Marina database connection.
// XAMPP commonly uses root with a blank password for local development.
// Environment variables can override the default connection settings when needed.
$host = getenv('MOFFAT_DB_HOST') ?: 'localhost';
$port = getenv('MOFFAT_DB_PORT') ?: '3306';
$db = getenv('MOFFAT_DB_NAME') ?: 'moffat_bay';
$user = getenv('MOFFAT_DB_USER') ?: 'root';

$pass = getenv('MOFFAT_DB_PASS');
if ($pass === false) {
    $pass = '';
}

$charset = 'utf8mb4';

$dsn = "mysql:host=$host;port=$port;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (PDOException $e) {
    http_response_code(500);
    header('Content-Type: application/json; charset=utf-8');
    echo json_encode([
        'ok' => false,
        'message' => 'Database connection failed. Check database.php settings and confirm MySQL is running.'
    ]);
    exit;
}

if (session_status() !== PHP_SESSION_ACTIVE) {
    session_start();
}
