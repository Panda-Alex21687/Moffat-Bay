import { useState } from "react";
import { Link } from "react-router";

const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

const DEMO_RESERVATION = {
  id: "MB-2026-00125",
  boatName: "Sea Explorer",
  boatLength: "34 ft",
  slipSize: "40 ft",
  slipNumber: "A-07",
  checkIn: "09/15/2026",
  status: "Confirmed",
};

const inputStyle: React.CSSProperties = {
  width: "100%",
  padding: "11px 14px",
  border: `1px solid ${border}`,
  borderRadius: 6,
  fontSize: 15,
  fontFamily: "Open Sans, Arial, sans-serif",
  color: "#172033",
};

function DetailGrid({ r }: { r: typeof DEMO_RESERVATION }) {
  const fields: [string, string][] = [
    ["Reservation ID", r.id],
    ["Boat Name", r.boatName],
    ["Boat Length", r.boatLength],
    ["Slip Size", r.slipSize],
    ["Slip Number", r.slipNumber],
    ["Check-In Date", r.checkIn],
  ];
  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 0 }}>
      {fields.map(([label, value], i, arr) => (
        <div key={label} style={{
          padding: "13px 0",
          borderBottom: i < arr.length - 2 ? `1px solid ${border}` : "none",
          paddingRight: i % 2 === 0 ? 28 : 0,
          borderRight: i % 2 === 0 ? `1px solid ${border}` : "none",
          paddingLeft: i % 2 !== 0 ? 28 : 0,
        }}>
          <div style={{ fontSize: 12, color: "#6b7a99", fontWeight: 600, marginBottom: 3 }}>{label}</div>
          <div style={{ fontSize: 15, color: navy, fontWeight: 600 }}>{value}</div>
        </div>
      ))}
      <div style={{ padding: "13px 0", gridColumn: "span 2" }}>
        <div style={{ fontSize: 12, color: "#6b7a99", fontWeight: 600, marginBottom: 3 }}>Status</div>
        <span style={{ background: "#dcfce7", color: "#166534", padding: "4px 12px", borderRadius: 12, fontSize: 13, fontWeight: 600 }}>
          {r.status}
        </span>
      </div>
    </div>
  );
}

export default function ReservationLookup() {
  const [rid, setRid] = useState("");
  const [email, setEmail] = useState("");
  const [result, setResult] = useState<"found" | "notfound" | null>(null);

  function search() {
    const ridMatch = rid.trim().toLowerCase() === DEMO_RESERVATION.id.toLowerCase();
    const emailMatch = email.trim().toLowerCase() === "customer@example.com";
    setResult(ridMatch || emailMatch ? "found" : "notfound");
  }

  return (
    <>
      <section style={{ background: navy, color: "#fff", padding: "60px 5%" }}>
        <div style={{ maxWidth: 900, margin: "auto" }}>
          <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>UST-08 • RESERVATION LOOKUP</div>
          <h1 style={{ fontSize: "clamp(32px,4.5vw,52px)", lineHeight: 1.1, marginBottom: 16 }}>Find Your Reservation</h1>
          <div style={{ width: 70, height: 4, background: gold }} />
        </div>
      </section>

      {/* Sub-nav */}
      <div style={{ background: "#fff", borderBottom: `1px solid ${border}` }}>
        <div style={{ maxWidth: 900, margin: "0 auto", padding: "0 5%", display: "flex" }}>
          {[
            { label: "RESERVATION", to: "/reservations", active: false },
            { label: "SUMMARY", to: "/reservation-summary", active: false },
            { label: "LOOKUP", to: "/reservation-lookup", active: true },
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

      <div style={{ maxWidth: 900, margin: "0 auto", padding: "48px 5%", display: "flex", flexDirection: "column", gap: 24 }}>
        {/* Search form */}
        <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)" }}>
          <h2 style={{ color: navy, fontSize: 20, marginBottom: 4 }}>Search Reservations</h2>
          <p style={{ color: "#6b7a99", fontSize: 14, marginBottom: 20 }}>Search using a reservation ID or customer email address.</p>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16, marginBottom: 20 }}>
            <div>
              <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6 }}>Reservation ID</label>
              <input value={rid} onChange={(e) => setRid(e.target.value)} style={inputStyle} placeholder="MB-2026-00125" />
            </div>
            <div>
              <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6 }}>Email Address</label>
              <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" style={inputStyle} placeholder="customer@example.com" />
            </div>
          </div>
          <button onClick={search} style={{
            background: gold, color: navy, border: "none",
            padding: "13px 26px",
            fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14, cursor: "pointer",
          }}>
            SEARCH RESERVATION
          </button>
        </section>

        {/* Search result */}
        {result === "found" && (
          <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)" }}>
            <h2 style={{ color: navy, fontSize: 20, marginBottom: 20 }}>Reservation Result</h2>
            <DetailGrid r={DEMO_RESERVATION} />
          </section>
        )}
        {result === "notfound" && (
          <div style={{ background: "#fef3c7", border: "1px solid #fcd34d", borderRadius: 8, padding: "16px 20px", fontSize: 14, color: "#92400e" }}>
            No reservation found. Check the reservation ID or email address and try again.
          </div>
        )}

        {/* Reservation history */}
        <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)" }}>
          <h2 style={{ color: navy, fontSize: 20, marginBottom: 20 }}>Reservation History</h2>
          <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 0 }}>
            {[
              ["Reservation ID", DEMO_RESERVATION.id],
              ["Check-In", DEMO_RESERVATION.checkIn],
              ["Slip", `${DEMO_RESERVATION.slipSize} • ${DEMO_RESERVATION.slipNumber}`],
            ].map(([label, value], i, arr) => (
              <div key={String(label)} style={{
                padding: "13px 0",
                borderBottom: i < arr.length - 2 ? `1px solid ${border}` : "none",
                paddingRight: i % 2 === 0 ? 28 : 0,
                borderRight: i % 2 === 0 ? `1px solid ${border}` : "none",
                paddingLeft: i % 2 !== 0 ? 28 : 0,
              }}>
                <div style={{ fontSize: 12, color: "#6b7a99", fontWeight: 600, marginBottom: 3 }}>{label}</div>
                <div style={{ fontSize: 15, color: navy, fontWeight: 600 }}>{value}</div>
              </div>
            ))}
            <div style={{ padding: "13px 0 0 28px" }}>
              <div style={{ fontSize: 12, color: "#6b7a99", fontWeight: 600, marginBottom: 3 }}>Status</div>
              <span style={{ background: "#dcfce7", color: "#166534", padding: "4px 12px", borderRadius: 12, fontSize: 13, fontWeight: 600 }}>
                {DEMO_RESERVATION.status}
              </span>
            </div>
          </div>
        </section>
      </div>
    </>
  );
}
