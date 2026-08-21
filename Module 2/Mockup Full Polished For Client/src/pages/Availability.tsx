const navy = "#061b3a";
const gold = "#eab53f";
const border = "#d9dde5";

const SLIP_DATA = [
  { size: 26, total: 30, reserved: 18, docks: "Slips 8–12 and 20–24 on each dock" },
  { size: 40, total: 24, reserved: 20, docks: "Slips 4–7 and 16–19 on each dock" },
  { size: 50, total: 18, reserved: 18, docks: "Slips 1–3 and 13–15 on each dock" },
];

export default function Availability() {
  return (
    <>
      <section style={{ background: navy, color: "#fff", padding: "60px 5%" }}>
        <div style={{ maxWidth: 1000, margin: "auto" }}>
          <div style={{ color: gold, fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, fontSize: 12, letterSpacing: 3, marginBottom: 12 }}>SLIP AVAILABILITY</div>
          <h1 style={{ fontSize: "clamp(32px,4.5vw,52px)", lineHeight: 1.1, marginBottom: 16 }}>Long-Term Slip Availability</h1>
          <div style={{ width: 70, height: 4, background: gold }} />
        </div>
      </section>

      <div style={{ maxWidth: 1000, margin: "0 auto", padding: "48px 5%", display: "flex", flexDirection: "column", gap: 28 }}>
        {/* Availability by slip size */}
        <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)" }}>
          <div style={{ marginBottom: 24 }}>
            <h2 style={{ color: navy, fontSize: 20, marginBottom: 4 }}>Availability by Slip Size</h2>
            <p style={{ color: "#6b7a99", fontSize: 14, margin: 0 }}>Inventory across Docks A, B, and C. Linear docks are for transient moorage only and are not shown.</p>
          </div>

          <div style={{ display: "grid", gridTemplateColumns: "repeat(3,1fr)", gap: 20 }}>
            {SLIP_DATA.map(({ size, total, reserved }) => {
              const avail = total - reserved;
              const pct = Math.round((reserved / total) * 100);
              const isFull = avail === 0;
              return (
                <div key={size} style={{
                  border: `1px solid ${border}`,
                  borderRadius: 8,
                  padding: "24px 20px",
                  borderTop: `4px solid ${isFull ? "#ef4444" : gold}`,
                }}>
                  <div style={{ fontFamily: "Montserrat, Arial, sans-serif", fontSize: 28, fontWeight: 700, color: navy, marginBottom: 2 }}>
                    {size} ft
                  </div>
                  <div style={{ fontSize: 13, color: "#6b7a99", marginBottom: 16 }}>Slip Size</div>

                  <div style={{ display: "flex", alignItems: "center", gap: 10, marginBottom: 12 }}>
                    {isFull
                      ? <span style={{ background: "#fee2e2", color: "#991b1b", padding: "4px 12px", borderRadius: 12, fontSize: 13, fontWeight: 600 }}>Fully Reserved</span>
                      : <span style={{ background: "#dcfce7", color: "#166534", padding: "4px 12px", borderRadius: 12, fontSize: 13, fontWeight: 600 }}>{avail} Available</span>
                    }
                  </div>

                  {/* Progress bar */}
                  <div style={{ height: 8, background: "#e5e7eb", borderRadius: 4, marginBottom: 10, overflow: "hidden" }}>
                    <div style={{ width: `${pct}%`, height: "100%", background: isFull ? "#ef4444" : gold, borderRadius: 4 }} />
                  </div>
                  <div style={{ fontSize: 13, color: "#6b7a99" }}>{reserved} of {total} reserved</div>
                </div>
              );
            })}
          </div>
        </section>

        {/* Dock layout */}
        <section style={{ background: "#fff", border: `1px solid ${border}`, borderRadius: 10, padding: "32px 28px", boxShadow: "0 2px 10px rgba(0,0,0,0.04)" }}>
          <h2 style={{ color: navy, fontSize: 20, marginBottom: 4 }}>Where the Slips Are</h2>
          <p style={{ color: "#6b7a99", fontSize: 14, marginBottom: 20 }}>Each dock (A, B, C) has 24 slips arranged the same way.</p>
          <div style={{ display: "flex", flexDirection: "column" }}>
            {[
              { label: "26 ft slips", value: "Slips 8–12 and 20–24 on each dock" },
              { label: "40 ft slips", value: "Slips 4–7 and 16–19 on each dock" },
              { label: "50 ft slips", value: "Slips 1–3 and 13–15 on each dock" },
              { label: "Linear docks", value: "Transient moorage only" },
            ].map(({ label, value }, i, arr) => (
              <div key={label} style={{
                display: "flex", justifyContent: "space-between", alignItems: "center",
                padding: "14px 0",
                borderBottom: i < arr.length - 1 ? `1px solid ${border}` : "none",
              }}>
                <span style={{ fontSize: 13, color: "#6b7a99", fontWeight: 600 }}>{label}</span>
                <span style={{ fontSize: 14, color: navy, fontWeight: 600 }}>{value}</span>
              </div>
            ))}
          </div>
        </section>

        <div style={{ background: "#eff6ff", border: "1px solid #bfdbfe", borderRadius: 8, padding: "14px 18px", fontSize: 14, color: "#1e40af" }}>
          <strong>Prototype:</strong> These counts are simulated for demonstration. The final site should pull from the project database.
        </div>
      </div>
    </>
  );
}
