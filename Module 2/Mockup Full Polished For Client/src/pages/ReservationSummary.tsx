import { useState } from "react";
import { Link } from "react-router";

const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

const reservation = {
  id: "MB-2026-00125",
  boatName: "Sea Explorer",
  boatLength: "34 ft",
  slipSize: "40 ft",
  slipNumber: "A-07",
  checkIn: "09/15/2026",
  status: "Pending Confirmation",
  monthlyCost: "$350 / month",
};

export default function ReservationSummary() {
  const [confirmed, setConfirmed] = useState(false);
  const [cancelled, setCancelled] = useState(false);

  return (
    <>
      <section style={{ background: navy, color: "#fff", padding: "60px 5%" }}>
        <div style={{ maxWidth: 900, margin: "auto" }}>
          <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>UST-07 • RESERVATION SUMMARY</div>
          <h1 style={{ fontSize: "clamp(32px,4.5vw,52px)", lineHeight: 1.1, marginBottom: 16 }}>Review Your Reservation</h1>
          <div style={{ width: 70, height: 4, background: gold }} />
        </div>
      </section>

      {/* Sub-nav */}
      <div style={{ background: "#fff", borderBottom: `1px solid ${border}` }}>
        <div style={{ maxWidth: 900, margin: "0 auto", padding: "0 5%", display: "flex" }}>
          {[
            { label: "RESERVATION", to: "/reservations", active: false },
            { label: "SUMMARY", to: "/reservation-summary", active: true },
            { label: "LOOKUP", to: "/reservation-lookup", active: false },
          ].map(({ label, to, active }) => (
            <Link key={to} to={to} style={{
              display: "block", padding: "14px 24px",
              fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 600, fontSize: 13,
              color: active ? navy : "#6b7a99",
              textDecoration: "none",
              borderBottom: active ? `3px solid ${gold}` : "3px solid transparent",
            }}>{label}</Link>
          ))}
        </div>
      </div>

      <div style={{ maxWidth: 900, margin: "0 auto", padding: "48px 5%", display: "flex", flexDirection: "column", gap: 20 }}>
        {confirmed && (
          <div style={{ background: "#dcfce7", border: "1px solid #6ee7b7", borderRadius: 8, padding: "16px 20px", color: "#065f46", fontSize: 14, fontWeight: 600 }}>
            ✓ Reservation {reservation.id} confirmed. A confirmation email has been sent.
          </div>
        )}
        {cancelled && (
          <div style={{ background: "#fee2e2", border: "1px solid #fca5a5", borderRadius: 8, padding: "16px 20px", color: "#991b1b", fontSize: 14, fontWeight: 600 }}>
            ✗ Reservation cancelled. You have been returned to the reservation page.
          </div>
        )}

        <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)" }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 24 }}>
            <div>
              <h2 style={{ color: navy, fontSize: 20, marginBottom: 4 }}>Reservation Details</h2>
              <p style={{ color: "#6b7a99", fontSize: 14, margin: 0 }}>Please review your information before confirming.</p>
            </div>
            <span style={{ fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 13, color: "#6b7a99", background: "#f4f7fb", border: `1px solid ${border}`, padding: "6px 14px", borderRadius: 6 }}>
              {reservation.id}
            </span>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 0 }}>
            {[
              ["Reservation ID", reservation.id],
              ["Boat Name", reservation.boatName],
              ["Boat Length", reservation.boatLength],
              ["Slip Size", reservation.slipSize],
              ["Slip Number", reservation.slipNumber],
              ["Check-In Date", reservation.checkIn],
              ["Reservation Status", reservation.status],
              ["Monthly Cost", reservation.monthlyCost, true],
            ].map(([label, value, isCost], i, arr) => (
              <div key={String(label)} style={{
                padding: "14px 0",
                borderBottom: i < arr.length - 2 ? `1px solid ${border}` : "none",
                paddingRight: i % 2 === 0 ? 32 : 0,
                borderRight: i % 2 === 0 ? `1px solid ${border}` : "none",
                paddingLeft: i % 2 !== 0 ? 32 : 0,
              }}>
                <div style={{ fontSize: 13, color: "#6b7a99", fontWeight: 600, marginBottom: 4 }}>{label}</div>
                <div style={{
                  fontSize: isCost ? 20 : 15, color: isCost ? gold : navy,
                  fontWeight: 700, fontFamily: isCost ? "Montserrat, Arial, sans-serif" : undefined,
                }}>{value}</div>
              </div>
            ))}
          </div>

          <div style={{ marginTop: 28, display: "flex", gap: 14 }}>
            <button
              onClick={() => { setConfirmed(true); setCancelled(false); }}
              style={{
                background: gold, color: navy, border: "none",
                padding: "13px 26px",
                fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14,
                cursor: "pointer",
              }}
            >
              CONFIRM RESERVATION
            </button>
            <button
              onClick={() => { setCancelled(true); setConfirmed(false); }}
              style={{
                background: "transparent", color: "#dc2626",
                border: "2px solid #dc2626",
                padding: "13px 26px",
                fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14,
                cursor: "pointer",
              }}
            >
              CANCEL RESERVATION
            </button>
          </div>
        </section>
      </div>
    </>
  );
}
