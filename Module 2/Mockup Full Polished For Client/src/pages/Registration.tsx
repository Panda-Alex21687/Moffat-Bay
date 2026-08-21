import { useState } from "react";

const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

const inputStyle: React.CSSProperties = {
  width: "100%",
  padding: "11px 14px",
  border: `1px solid ${border}`,
  borderRadius: 6,
  fontSize: 15,
  fontFamily: "Open Sans, Arial, sans-serif",
  color: "#172033",
  outline: "none",
};

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6, color: "#172033" }}>{label}</label>
      {children}
    </div>
  );
}

function SectionHeading({ step, title, desc }: { step: string; title: string; desc: string }) {
  return (
    <div style={{ display: "flex", gap: 16, alignItems: "flex-start", marginBottom: 24 }}>
      <span style={{
        minWidth: 36, height: 36, borderRadius: "50%",
        background: navy, color: "#fff",
        display: "grid", placeItems: "center",
        fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14,
      }}>{step}</span>
      <div>
        <h3 style={{ color: navy, fontSize: 17, margin: "0 0 2px" }}>{title}</h3>
        <p style={{ color: "#6b7a99", fontSize: 14, margin: 0 }}>{desc}</p>
      </div>
    </div>
  );
}

export default function Registration() {
  const [submitted, setSubmitted] = useState(false);
  const [password, setPassword] = useState("");

  const strength = password.length === 0 ? 0
    : password.length < 6 ? 1
    : password.length < 10 ? 2
    : /[A-Z]/.test(password) && /[0-9]/.test(password) && /[^a-zA-Z0-9]/.test(password) ? 4
    : 3;

  const strengthColor = ["", "#ef4444", "#f97316", "#eab53f", "#22c55e"][strength];
  const strengthLabel = ["", "Too short", "Weak", "Fair", "Strong"][strength];

  if (submitted) {
    return (
      <div style={{ maxWidth: 600, margin: "80px auto", padding: "0 5%", textAlign: "center" }}>
        <div style={{ fontSize: 56, marginBottom: 16 }}>⚓</div>
        <h2 style={{ color: navy, fontSize: 28, marginBottom: 12 }}>Account Created!</h2>
        <p style={{ color: "#4a5568", marginBottom: 28 }}>Your marina account has been created. You can now proceed to reserve a slip.</p>
        <a href="/reservations" style={{ display: "inline-block", background: gold, color: navy, padding: "14px 28px", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, textDecoration: "none" }}>
          RESERVE A SLIP →
        </a>
      </div>
    );
  }

  return (
    <>
      <section style={{ background: navy, color: "#fff", padding: "50px 5%" }}>
        <div style={{ maxWidth: 1100, margin: "auto" }}>
          <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>NEW CUSTOMER REGISTRATION</div>
          <h1 style={{ fontSize: "clamp(28px,4vw,46px)", lineHeight: 1.1, marginBottom: 16 }}>Create Your Marina Account</h1>
          <div style={{ width: 70, height: 4, background: gold, marginBottom: 18 }} />
          <p style={{ color: "#c8d4e8", fontSize: 16 }}>Enter your customer and boat information once, then continue directly into the slip reservation process.</p>
        </div>
      </section>

      {/* Workflow strip */}
      <div style={{ background: "#fff", borderBottom: `1px solid ${border}` }}>
        <div style={{ maxWidth: 1100, margin: "0 auto", padding: "18px 5%", display: "flex", gap: 0 }}>
          {[
            { n: "1", label: "Create Account", sub: "Customer information", done: true },
            { n: "2", label: "Add Boat", sub: "Name and length", done: true },
            { n: "3", label: "Reserve Slip", sub: "Choose reservation details", done: false },
          ].map(({ n, label, sub, done }, i) => (
            <div key={n} style={{ display: "flex", alignItems: "center", gap: 0 }}>
              <div style={{ display: "flex", alignItems: "center", gap: 12, padding: "0 24px" }}>
                <div style={{
                  width: 34, height: 34, borderRadius: "50%",
                  background: done ? navy : "#e5e7eb",
                  color: done ? "#fff" : "#9ca3af",
                  display: "grid", placeItems: "center",
                  fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14,
                }}>{n}</div>
                <div>
                  <div style={{ fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14, color: done ? navy : "#9ca3af" }}>{label}</div>
                  <div style={{ fontSize: 12, color: "#9ca3af" }}>{sub}</div>
                </div>
              </div>
              {i < 2 && <div style={{ width: 40, height: 2, background: "#e5e7eb" }} />}
            </div>
          ))}
        </div>
      </div>

      <section style={{ maxWidth: 1100, margin: "0 auto", padding: "48px 5%", display: "grid", gridTemplateColumns: "320px 1fr", gap: 48 }}>
        {/* Sidebar */}
        <aside>
          <div style={{ color: "#c8921e", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 2, marginBottom: 10 }}>EASY SIGN-UP</div>
          <h2 style={{ color: navy, fontSize: 26, lineHeight: 1.2, marginBottom: 14 }}>One Account.<br />One Simple Process.</h2>
          <div style={{ width: 50, height: 3, background: gold, marginBottom: 20 }} />
          <p style={{ color: "#4a5568", fontSize: 14, marginBottom: 20 }}>
            Your account keeps your contact and boat information together so you do not have to enter the same details every time you begin a reservation.
          </p>
          <div style={{ background: "#f4f7fb", border: `1px solid ${border}`, borderRadius: 8, padding: "20px", marginBottom: 16 }}>
            <strong style={{ fontSize: 14, color: navy, display: "block", marginBottom: 10 }}>Password requirements</strong>
            <ul style={{ fontSize: 13, color: "#4a5568", paddingLeft: 18, margin: 0, display: "flex", flexDirection: "column", gap: 6 }}>
              <li>At least 8 characters</li>
              <li>One uppercase and one lowercase letter</li>
              <li>One number</li>
              <li>One special character</li>
            </ul>
          </div>
          <p style={{ fontSize: 13, color: "#6b7a99" }}>Passwords are handled on the server and stored as secure hashes rather than plain text.</p>
        </aside>

        {/* Form card */}
        <form
          onSubmit={(e) => { e.preventDefault(); setSubmitted(true); }}
          style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "36px 32px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)", display: "flex", flexDirection: "column", gap: 0 }}
        >
          {/* Customer Info */}
          <SectionHeading step="01" title="Customer Information" desc="Tell us who will be responsible for the marina account." />
          <div style={{ display: "flex", flexDirection: "column", gap: 16, marginBottom: 28 }}>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
              <Field label="First Name *"><input required style={inputStyle} /></Field>
              <Field label="Last Name *"><input required style={inputStyle} /></Field>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
              <Field label="Phone Number *"><input required type="tel" style={inputStyle} placeholder="(555) 123-4567" /></Field>
              <Field label="Street Address *"><input required style={inputStyle} /></Field>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr 1fr", gap: 16 }}>
              <Field label="City *"><input required style={inputStyle} /></Field>
              <Field label="State *"><input required style={inputStyle} maxLength={2} placeholder="WA" /></Field>
              <Field label="ZIP Code *"><input required style={inputStyle} placeholder="98250" /></Field>
            </div>
          </div>

          <div style={{ borderTop: `1px solid ${border}`, margin: "0 0 28px" }} />

          {/* Account Login */}
          <SectionHeading step="02" title="Account Login" desc="Your email address will be used as your account username." />
          <div style={{ display: "flex", flexDirection: "column", gap: 16, marginBottom: 28 }}>
            <Field label="Email Address *"><input required type="email" style={inputStyle} placeholder="name@example.com" /></Field>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
              <div>
                <Field label="Password *">
                  <input required type="password" style={inputStyle} value={password} onChange={(e) => setPassword(e.target.value)} />
                </Field>
                {password.length > 0 && (
                  <div style={{ marginTop: 8 }}>
                    <div style={{ height: 4, background: "#e5e7eb", borderRadius: 2, overflow: "hidden" }}>
                      <div style={{ width: `${(strength / 4) * 100}%`, height: "100%", background: strengthColor, transition: "width 0.2s, background 0.2s", borderRadius: 2 }} />
                    </div>
                    <div style={{ fontSize: 12, color: strengthColor, marginTop: 4 }}>{strengthLabel}</div>
                  </div>
                )}
              </div>
              <Field label="Confirm Password *"><input required type="password" style={inputStyle} /></Field>
            </div>
          </div>

          <div style={{ borderTop: `1px solid ${border}`, margin: "0 0 28px" }} />

          {/* Boat Info */}
          <SectionHeading step="03" title="Boat Information" desc="Add the boat that will be connected to your slip reservation." />
          <div style={{ display: "flex", flexDirection: "column", gap: 16, marginBottom: 28 }}>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
              <Field label="Boat Name *"><input required style={inputStyle} placeholder="Example: Sea Breeze" /></Field>
              <Field label="Boat Length (feet) *"><input required type="number" min={1} max={200} style={inputStyle} placeholder="30" /></Field>
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
              <Field label="Boat Type">
                <select style={{ ...inputStyle, background: "#fff" }}>
                  <option value="">Select type</option>
                  <option>Sailboat</option>
                  <option>Powerboat</option>
                  <option>Fishing Boat</option>
                  <option>Yacht</option>
                  <option>Other</option>
                </select>
              </Field>
              <Field label="Registration Number"><input style={inputStyle} placeholder="Optional" /></Field>
            </div>
          </div>

          <label style={{ display: "flex", gap: 10, alignItems: "flex-start", marginBottom: 24, fontSize: 14, color: "#172033", cursor: "pointer" }}>
            <input type="checkbox" required style={{ marginTop: 3 }} />
            <span>I confirm that the information entered above is accurate.</span>
          </label>

          <button type="submit" style={{
            background: gold, color: navy, border: "none",
            padding: "16px 28px",
            fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 15,
            cursor: "pointer", letterSpacing: 0.5,
          }}>
            CREATE ACCOUNT & CONTINUE →
          </button>
          <p style={{ fontSize: 13, color: "#6b7a99", textAlign: "center", marginTop: 14 }}>
            Already registered? <a href="/login" style={{ color: navy, fontWeight: 700 }}>Sign in here</a>.
          </p>
        </form>
      </section>
    </>
  );
}
