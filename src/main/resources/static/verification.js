document.addEventListener("DOMContentLoaded", function () {

    const verificationPending =
        document.getElementById("verificationPending");

    const verificationComplete =
        document.getElementById("verificationComplete");

    const verificationNotice =
        document.getElementById("verificationNotice");

    const verifyBtn =
        document.getElementById("verifyBtn");

    const customerId =
        document.getElementById("customerId");

    const params =
        new URLSearchParams(window.location.search);

    const token =
        params.get("token");

    if (!token) {

        verificationNotice.textContent =
            "No verification token was found in this link.";

        verifyBtn.disabled = true;

        return;
    }

    verifyBtn.addEventListener("click", verifyEmail);

    // If the customer opened the page directly from
    // the verification email, verify automatically.
    verifyEmail();


    async function verifyEmail() {

        verifyBtn.disabled = true;

        verificationNotice.textContent =
            "Verifying your email...";

        try {

            const response = await fetch(
                "/verification?token=" +
                encodeURIComponent(token)
            );

            const data = await response.json();

            if (!response.ok || !data.ok) {

                throw new Error(
                    data.message ||
                    "Email verification failed."
                );
            }

            if (data.customerId) {

                customerId.textContent =
                    data.customerId;
            }

            verificationPending.classList.add("hidden");

            verificationComplete.classList.remove("hidden");

        } catch (error) {

            verificationNotice.textContent =
                error.message ||
                "Email verification could not be completed.";

            verifyBtn.disabled = false;
        }
    }
});