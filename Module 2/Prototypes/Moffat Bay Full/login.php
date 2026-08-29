<?php
require_once __DIR__ . '/database.php';

$error = '';
$email = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email = trim($_POST['email'] ?? '');
    $password = $_POST['password'] ?? '';

    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = 'Enter a valid email address.';
    } elseif ($password === '') {
        $error = 'Enter your password.';
    } else {
        $stmt = $pdo->prepare(
            'SELECT customer_id, first_name, last_name, email, password_hash, email_verified
             FROM customers
             WHERE email = :email
             LIMIT 1'
        );
        $stmt->execute(['email' => $email]);
        $customer = $stmt->fetch();

        if (!$customer || !password_verify($password, $customer['password_hash'])) {
            $error = 'Invalid email or password.';
        } elseif (!(bool) $customer['email_verified']) {
            $error = 'Please verify your email address before logging in.';
        } else {
            session_regenerate_id(true);

            $_SESSION['customer_id'] = (int) $customer['customer_id'];
            $_SESSION['first_name'] = $customer['first_name'];
            $_SESSION['last_name'] = $customer['last_name'];
            $_SESSION['email'] = $customer['email'];

            header('Location: post_login.html');
            exit;
        }
    }
}
?>
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    >

    <title>Login - Moffat Bay Marina</title>

    <link
        rel="preconnect"
        href="https://fonts.googleapis.com"
    >

    <link
        rel="preconnect"
        href="https://fonts.gstatic.com"
        crossorigin
    >

    <link
        href="https://fonts.googleapis.com/css2?family=Montserrat:wght@500;600;700&family=Open+Sans:wght@400;600&display=swap"
        rel="stylesheet"
    >

    <link
        rel="stylesheet"
        href="styles.css"
    >
</head>

<body>

    <header class="site-header">

        <div class="header-inner">

            <a
                class="brand"
                href="index.html"
                aria-label="Moffat Bay Marina home"
            >

                <span
                    class="anchor-logo"
                    aria-hidden="true"
                >
                    ⚓
                </span>

                <span>

                    <span class="brand-name">
                        MOFFAT BAY
                    </span>

                    <span class="brand-subtitle">
                        MARINA
                    </span>

                </span>

            </a>

            <nav aria-label="Main navigation">

                <ul>

                    <li>
                        <a href="index.html">
                            HOME
                        </a>
                    </li>

                    <li>
                        <a href="about.html">
                            ABOUT US
                        </a>
                    </li>

                    <li>
                        <a href="contact.html">
                            CONTACT US
                        </a>
                    </li>

                    <li>
                        <a href="register.html">
                            REGISTER
                        </a>
                    </li>

                </ul>

            </nav>

        </div>

    </header>

    <main>

        <section class="hero-compact">

            <div>

                <div class="eyebrow">
                    UST-04 - Login / authentication
                </div>

                <h1>
                    Welcome back
                </h1>

                <p class="lead">
                    Registered customers can log in with their email
                    address and password to access their account.
                </p>

            </div>

            <aside class="prototype-badge">

                <strong>
                    Secure customer login
                </strong>

                <p>
                    Passwords are verified against the stored password
                    hash and a server-side session is created after a
                    successful login.
                </p>

            </aside>

        </section>

        <section
            class="card center-card"
            id="loginCard"
            style="text-align: left;"
        >

            <?php if ($error !== ''): ?>
                <div
                    class="notice error"
                    role="alert"
                >
                    <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
                </div>
            <?php endif; ?>

            <form
                id="loginForm"
                method="post"
                action="login.php"
            >

                <div class="stack">

                    <div>

                        <label for="loginEmail">
                            Email address / username
                        </label>

                        <input
                            id="loginEmail"
                            name="email"
                            type="email"
                            placeholder="name@example.com"
                            autocomplete="email"
                            value="<?= htmlspecialchars($email, ENT_QUOTES, 'UTF-8') ?>"
                            required
                        >

                    </div>

                    <div>

                        <label for="loginPassword">
                            Password
                        </label>

                        <input
                            id="loginPassword"
                            name="password"
                            type="password"
                            autocomplete="current-password"
                            required
                        >

                    </div>

                    <button
                        class="btn primary full"
                        type="submit"
                    >
                        Log In
                    </button>

                    <a
                        class="btn secondary full"
                        href="register.html"
                    >
                        Create a free account
                    </a>

                </div>

            </form>

        </section>

    </main>

    <footer>
        Moffat Bay Marina • CSD 460 Module 2 Green Team Prototype
    </footer>

</body>

</html>
