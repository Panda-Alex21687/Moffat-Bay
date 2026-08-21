import { Link, useLocation, Outlet } from "react-router";

const navLinks = [
  { to: "/", label: "HOME" },
  { to: "/about", label: "ABOUT US" },
  { to: "/contact", label: "CONTACT US" },
  { to: "/pricing", label: "PRICING" },
  { to: "/availability", label: "AVAILABILITY" },
  { to: "/reservations", label: "RESERVATIONS" },
  { to: "/login", label: "LOGIN" },
];

export default function Layout() {
  const { pathname } = useLocation();

  return (
    <div style={{ display: "flex", flexDirection: "column", minHeight: "100vh" }}>
      <header style={{ background: "#061b3a", color: "#fff", position: "sticky", top: 0, zIndex: 10 }}>
        <div style={{ maxWidth: 1400, margin: "auto", minHeight: 88, padding: "0 5%", display: "flex", alignItems: "center", justifyContent: "space-between", gap: 24 }}>
          <Link to="/" style={{ display: "flex", alignItems: "center", gap: 14, textDecoration: "none", color: "#fff" }}>
            <span style={{ fontSize: 36 }}>⚓</span>
            <span>
              <span style={{ fontFamily: "Montserrat, Arial, sans-serif", fontSize: 22, fontWeight: 700, letterSpacing: 1 }}>MOFFAT BAY</span>
              <span style={{ display: "block", color: "#eab53f", fontSize: 12, letterSpacing: 5, marginTop: -2 }}>MARINA</span>
            </span>
          </Link>
          <nav>
            <ul style={{ listStyle: "none", display: "flex", gap: 32, margin: 0, padding: 0, flexWrap: "wrap" }}>
              {navLinks.map(({ to, label }) => {
                const isActive = to === "/" ? pathname === "/" : pathname.startsWith(to);
                return (
                  <li key={to}>
                    <Link
                      to={to}
                      style={{
                        color: isActive ? "#eab53f" : "#fff",
                        textDecoration: "none",
                        fontFamily: "Montserrat, Arial, sans-serif",
                        fontSize: 14,
                        fontWeight: 600,
                        padding: "10px 0",
                        borderBottom: isActive ? "2px solid #eab53f" : "2px solid transparent",
                        transition: "color 0.15s",
                      }}
                    >
                      {label}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </nav>
        </div>
      </header>

      <main style={{ flex: 1 }}>
        <Outlet />
      </main>

      <footer style={{ background: "#061b3a", color: "#fff" }}>
        <div style={{ maxWidth: 1400, margin: "auto", padding: "36px 5%", display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 32 }}>
          {[
            { icon: "📍", title: "VISIT US", lines: ["123 Harbor Way", "Moffat Bay, ST 12345"] },
            { icon: "📞", title: "CALL US", lines: ["(555) 123-4567", "Mon – Fri: 8am – 5pm"] },
            { icon: "📡", title: "VHF CHANNEL", lines: ["Channel 16", "Harbormaster"] },
            { icon: "✉", title: "EMAIL US", lines: ["info@moffatbaymarina.com", "We're here to help!"] },
          ].map(({ icon, title, lines }) => (
            <div key={title} style={{ display: "flex", gap: 14, alignItems: "flex-start" }}>
              <span style={{ color: "#eab53f", fontSize: 22, marginTop: 2 }}>{icon}</span>
              <div>
                <div style={{ fontFamily: "Montserrat, Arial, sans-serif", fontSize: 13, fontWeight: 700, marginBottom: 4 }}>{title}</div>
                {lines.map((l, i) => <div key={i} style={{ fontSize: 13, color: "#c8d4e8" }}>{l}</div>)}
              </div>
            </div>
          ))}
        </div>
        <div style={{ borderTop: "1px solid rgba(255,255,255,0.12)", textAlign: "center", padding: "14px", fontSize: 12, color: "#8fa3c0" }}>
          © 2026 Moffat Bay Marina. Prototype for CSD 460.
        </div>
      </footer>
    </div>
  );
}
