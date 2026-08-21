import { useState } from "react";

const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

export default function Contact() {
  const [submitted, setSubmitted] = useState(false);

  const inputStyle = {
    width: "100%",
    padding: "11px 14px",
    border: `1px solid ${border}`,
    borderRadius: 6,
    fontSize: 15,
    fontFamily: "Open Sans, Arial, sans-serif",
    outline: "none",
    color: "#172033",
  };

  return (
    <>
      {/* Page hero */}
      <section style={{ background: navy, color: "#fff", padding: "60px 5%" }}>
        <div style={{ maxWidth: 1200, margin: "auto" }}>
          <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>GET IN TOUCH</div>
          <h1 style={{ fontSize: "clamp(32px,4.5vw,52px)", lineHeight: 1.1, marginBottom: 16 }}>Contact Moffat Bay Marina</h1>
          <div style={{ width: 70, height: 4, background: gold, marginBottom: 18 }} />
          <p style={{ color: "#c8d4e8", fontSize: 17, maxWidth: 560 }}>
            Questions about the marina, amenities, or long-term slip reservations? Our contact information is available publicly so visitors can reach us without signing in.
          </p>
        </div>
      </section>

      {/* Contact grid */}
      <section style={{ maxWidth: 1200, margin: "0 auto", padding: "70px 5%", display: "grid", gridTemplateColumns: "1fr 1.3fr", gap: 64 }}>
        <div>
          <div style={{ color: "#c8921e", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 2, marginBottom: 10 }}>CONTACT INFORMATION</div>
          <h2 style={{ color: navy, fontSize: "clamp(24px,3vw,36px)", marginBottom: 14 }}>We're Here to Help</h2>
          <div style={{ width: 50, height: 3, background: gold, marginBottom: 20 }} />
          <p style={{ color: "#4a5568", marginBottom: 28 }}>
            Use any of the contact options below to reach Moffat Bay Marina. These details are available to all visitors without requiring an account.
          </p>
          <div style={{ display: "flex", flexDirection: "column", gap: 20 }}>
            {[
              { icon: "📍", title: "Visit Us", lines: ["123 Harbor Way", "Moffat Bay, ST 12345"] },
              { icon: "📞", title: "Call Us", lines: ["(555) 123-4567", "Monday – Friday: 8:00 AM – 5:00 PM"] },
              { icon: "📡", title: "VHF Channel", lines: ["Channel 16", "Harbormaster"] },
              { icon: "✉", title: "Email Us", lines: ["info@moffatbaymarina.com"] },
            ].map(({ icon, title, lines }) => (
              <div key={title} style={{ display: "flex", gap: 16, alignItems: "flex-start" }}>
                <div style={{
                  width: 46, height: 46, minWidth: 46, borderRadius: "50%",
                  background: "#f4f7fb",
                  border: `1px solid ${border}`,
                  display: "grid", placeItems: "center", fontSize: 20,
                }}>{icon}</div>
                <div>
                  <div style={{ fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14, color: navy, marginBottom: 2 }}>{title}</div>
                  {lines.map((l, i) => <div key={i} style={{ fontSize: 14, color: "#4a5568" }}>{l}</div>)}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Contact form */}
        <div style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "36px 32px", boxShadow: "0 4px 16px rgba(0,0,0,0.06)" }}>
          <h3 style={{ color: navy, fontSize: 20, marginBottom: 6 }}>Send Us a Message</h3>
          <p style={{ color: "#6b7a99", fontSize: 14, marginBottom: 24 }}>Front-end contact form. Server-side submission can be connected later if required.</p>

          {submitted ? (
            <div style={{ background: "#ecfdf5", border: "1px solid #6ee7b7", borderRadius: 8, padding: "20px 24px", color: "#065f46", fontSize: 15 }}>
              ✓ Thank you for your message. We'll be in touch soon.
            </div>
          ) : (
            <form onSubmit={(e) => { e.preventDefault(); setSubmitted(true); }} style={{ display: "flex", flexDirection: "column", gap: 18 }}>
              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
                <div>
                  <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6, color: "#172033" }}>First Name *</label>
                  <input required style={inputStyle} />
                </div>
                <div>
                  <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6, color: "#172033" }}>Last Name *</label>
                  <input required style={inputStyle} />
                </div>
              </div>
              <div>
                <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6, color: "#172033" }}>Email Address *</label>
                <input required type="email" style={inputStyle} />
              </div>
              <div>
                <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6, color: "#172033" }}>Phone Number</label>
                <input type="tel" style={inputStyle} placeholder="(555) 123-4567" />
              </div>
              <div>
                <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6, color: "#172033" }}>Message *</label>
                <textarea required rows={5} style={{ ...inputStyle, resize: "vertical" }} />
              </div>
              <button type="submit" style={{
                background: gold,
                color: navy,
                border: "none",
                padding: "14px 28px",
                fontFamily: "Montserrat, Arial, sans-serif",
                fontWeight: 700,
                fontSize: 14,
                cursor: "pointer",
                letterSpacing: 0.5,
              }}>
                SEND MESSAGE
              </button>
            </form>
          )}
        </div>
      </section>
    </>
  );
}
