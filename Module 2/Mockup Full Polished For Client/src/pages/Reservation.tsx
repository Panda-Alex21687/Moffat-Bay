import { Link } from "react-router";

const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

const reservation = {
  boatName: "Sea Explorer",
  boatLength: "34 ft",
  slipSize: "40 ft",
  checkIn: "09/15/2026",
};

export default function Reservation() {
  return (
    <>
      <section style={{ background: navy, color: "#fff", padding: "60px 5%" }}>
        <div style={{ maxWidth: 900, margin: "auto" }}>
          <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>RESERVATION FLOW</div>
          <h1 style={{ fontSize: "clamp(32px,4.5vw,52px)", lineHeight: 1.1, marginBottom: 16 }}>Reserve a Slip</h1>
          <div style={{ width: 70, height: 4, background: gold }} />
        </div>
      </section>

      {/* Sub-nav */}
      <div style={{ background: "#fff", borderBottom: `1px solid ${border}` }}>
        <div style={{ maxWidth: 900, margin: "0 auto", padding: "0 5%", display: "flex", gap: 0 }}>
          {[
            { label: "RESERVATION", to: "/reservations", active: true },
            { label: "SUMMARY", to: "/reservation-summary", active: false },
            { label: "LOOKUP", to: "/reservation-lookup", active: false },
          ].map(({ label, to, active }) => (
            <Link key={to} to={to} style={{
              display: "block", padding: "14px 24px",
              fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 600, fontSize: 13,
              color: active ? navy : "#6b7a99",
              textDecoration: "none",
              borderBottom: active ? `3px solid ${gold}` : "3px solid transparent",
            }}>
              {label}
            </Link>
          ))}
        </div>
      </div>

      <div style={{ maxWidth: 900, margin: "0 auto", padding: "48px 5%" }}>
        <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)" }}>
          <h2 style={{ color: navy, fontSize: 20, marginBottom: 4 }}>Reservation Information</h2>
          <p style={{ color: "#6b7a99", fontSize: 14, marginBottom: 24 }}>This page is the return destination after a reservation is cancelled.</p>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 0 }}>
            {[
              ["Boat Name", reservation.boatName],
              ["Boat Length", reservation.boatLength],
              ["Required Slip Size", reservation.slipSize],
              ["Check-In Date", reservation.checkIn],
            ].map(([label, value], i, arr) => (
              <div key={label} style={{
                padding: "14px 0",
                borderBottom: i < arr.length - 2 ? `1px solid ${border}` : "none",
                paddingRight: i % 2 === 0 ? 32 : 0,
                borderRight: i % 2 === 0 ? `1px solid ${border}` : "none",
                paddingLeft: i % 2 !== 0 ? 32 : 0,
              }}>
                <div style={{ fontSize: 13, color: "#6b7a99", fontWeight: 600, marginBottom: 4 }}>{label}</div>
                <div style={{ fontSize: 16, color: navy, fontWeight: 600 }}>{value}</div>
              </div>
            ))}
          </div>

          <div style={{ marginTop: 28, display: "flex", gap: 14 }}>
            <Link to="/reservation-summary" style={{
              display: "inline-block",
              background: gold, color: navy,
              padding: "13px 26px",
              fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14,
              textDecoration: "none",
            }}>
              CONTINUE TO SUMMARY
            </Link>
            <Link to="/availability" style={{
              display: "inline-block",
              background: "transparent", color: navy,
              border: `2px solid ${navy}`,
              padding: "13px 26px",
              fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14,
              textDecoration: "none",
            }}>
              CHECK AVAILABILITY
            </Link>
          </div>
        </section>
      </div>
    </>
  );
}
