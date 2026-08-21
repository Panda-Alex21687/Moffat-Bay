import { useState } from "react";

const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

const SLIP_SIZES = [26, 40, 50];
const SLIP_INVENTORY: Record<number, number> = { 26: 30, 40: 24, 50: 18 };

function requiredSlipSize(boatLen: number): number | null {
  for (const s of SLIP_SIZES) if (boatLen <= s) return s;
  return null;
}

function availableSlips(size: number): number {
  return Math.max(0, SLIP_INVENTORY[size] - Math.floor(Math.random() * 10 + 10));
}

interface Estimate {
  boatLen: number;
  slipSize: number;
  rate: number;
  avail: number;
  cost: number;
}

const Card = ({ children, style }: { children: React.ReactNode; style?: React.CSSProperties }) => (
  <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)", ...style }}>
    {children}
  </section>
);

const Label = ({ children }: { children: React.ReactNode }) => (
  <span style={{ fontSize: 13, color: "#6b7a99", fontWeight: 600 }}>{children}</span>
);
const Value = ({ children, bold }: { children: React.ReactNode; bold?: boolean }) => (
  <span style={{ fontSize: bold ? 20 : 15, color: bold ? gold : navy, fontWeight: bold ? 700 : 600, fontFamily: bold ? "Montserrat, Arial, sans-serif" : undefined }}>{children}</span>
);

function GridItem({ label, value, bold }: { label: string; value: React.ReactNode; bold?: boolean }) {
  return (
    <div style={{ padding: "14px 0", borderBottom: `1px solid ${border}`, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
      <Label>{label}</Label>
      <Value bold={bold}>{value}</Value>
    </div>
  );
}

export default function Pricing() {
  const [boatLen, setBoatLen] = useState("");
  const [estimate, setEstimate] = useState<Estimate | null>(null);
  const [error, setError] = useState("");

  function calculate() {
    const len = parseInt(boatLen, 10);
    if (isNaN(len) || len <= 0) { setError("Please enter a valid boat length."); setEstimate(null); return; }
    const slipSize = requiredSlipSize(len);
    if (!slipSize) { setError("Our largest slips are 50 ft. Boats over 50 ft cannot be accommodated — please contact the harbormaster."); setEstimate(null); return; }
    setError("");
    setEstimate({ boatLen: len, slipSize, rate: len * 10, avail: availableSlips(slipSize), cost: len * 10 + 10 });
  }

  return (
    <>
      <section style={{ background: navy, color: "#fff", padding: "60px 5%" }}>
        <div style={{ maxWidth: 1000, margin: "auto" }}>
          <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>SLIP PRICING</div>
          <h1 style={{ fontSize: "clamp(32px,4.5vw,52px)", lineHeight: 1.1, marginBottom: 16 }}>Estimate Your Monthly Cost</h1>
          <div style={{ width: 70, height: 4, background: gold }} />
        </div>
      </section>

      <div style={{ maxWidth: 1000, margin: "0 auto", padding: "48px 5%", display: "flex", flexDirection: "column", gap: 28 }}>
        {/* Estimator */}
        <Card>
          <div style={{ marginBottom: 20 }}>
            <h2 style={{ color: navy, fontSize: 20, marginBottom: 4 }}>Cost Estimator</h2>
            <p style={{ color: "#6b7a99", fontSize: 14, margin: 0 }}>Monthly rate is $10 per foot of boat length plus $10 for electric power. Boats are assigned the smallest slip that fits.</p>
          </div>
          <div style={{ display: "grid", gridTemplateColumns: "1fr auto", gap: 16, alignItems: "flex-end", maxWidth: 480 }}>
            <div>
              <label style={{ display: "block", fontSize: 13, fontWeight: 600, marginBottom: 6, color: "#172033" }}>Boat Length (feet)</label>
              <input
                type="number" min={1} max={60} placeholder="34"
                value={boatLen}
                onChange={(e) => setBoatLen(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && calculate()}
                style={{ width: "100%", padding: "12px 14px", border: `1px solid ${border}`, borderRadius: 6, fontSize: 15 }}
              />
            </div>
            <button
              onClick={calculate}
              style={{ background: gold, color: navy, border: "none", padding: "12px 24px", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14, cursor: "pointer", borderRadius: 0, height: 47 }}
            >
              CALCULATE
            </button>
          </div>
          {error && <div style={{ marginTop: 16, background: "#fef3c7", border: "1px solid #fcd34d", borderRadius: 6, padding: "12px 16px", fontSize: 14, color: "#92400e" }}>{error}</div>}
        </Card>

        {/* Estimate result */}
        {estimate && (
          <Card>
            <h2 style={{ color: navy, fontSize: 20, marginBottom: 20 }}>Your Estimate</h2>
            <GridItem label="Boat Length" value={`${estimate.boatLen} ft`} />
            <GridItem label="Required Slip Size" value={`${estimate.slipSize} ft`} />
            <GridItem label="Slip Rate ($10/ft)" value={`$${estimate.rate} / month`} />
            <GridItem label="Electric Power" value="$10 / month" />
            <GridItem label="Slips Available" value={
              estimate.avail > 0
                ? <span style={{ background: "#dcfce7", color: "#166534", padding: "3px 10px", borderRadius: 12, fontSize: 13, fontWeight: 600 }}>{estimate.avail} available</span>
                : <span style={{ background: "#fee2e2", color: "#991b1b", padding: "3px 10px", borderRadius: 12, fontSize: 13, fontWeight: 600 }}>Fully reserved</span>
            } />
            <GridItem label="Estimated Monthly Cost" value={`$${estimate.cost} / month`} bold />
            <div style={{ marginTop: 20 }}>
              {estimate.avail > 0 ? (
                <a href="/reservations" style={{ display: "inline-block", background: gold, color: navy, padding: "13px 26px", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 14, textDecoration: "none" }}>
                  RESERVE A SLIP
                </a>
              ) : (
                <div style={{ background: "#fef3c7", border: "1px solid #fcd34d", borderRadius: 6, padding: "14px 18px", fontSize: 14, color: "#92400e" }}>
                  <strong>No {estimate.slipSize} ft slips available.</strong> You can join the wait list and we will contact you when a slip opens up.
                </div>
              )}
            </div>
          </Card>
        )}

        {/* Sample rates */}
        <Card>
          <h2 style={{ color: navy, fontSize: 20, marginBottom: 4 }}>Sample Monthly Rates</h2>
          <p style={{ color: "#6b7a99", fontSize: 14, marginBottom: 20 }}>Cost is based on your boat length, not the slip size. Electric power included in examples below.</p>
          <GridItem label="26 ft slip • 26 ft boat" value="$270 / month" />
          <GridItem label="40 ft slip • 34 ft boat" value="$350 / month" />
          <GridItem label="40 ft slip • 40 ft boat" value="$410 / month" />
          <GridItem label="50 ft slip • 50 ft boat" value="$510 / month" />
        </Card>

        <div style={{ background: "#eff6ff", border: "1px solid #bfdbfe", borderRadius: 8, padding: "14px 18px", fontSize: 14, color: "#1e40af" }}>
          <strong>Prototype:</strong> Availability numbers are simulated for demonstration. Database integration will be added in a later sprint.
        </div>
      </div>
    </>
  );
}
