import { Link } from "react-router";

const HERO_IMG = "https://images.unsplash.com/photo-1777608598994-0f4de754622c?w=1600&h=700&fit=crop&auto=format";
const WELCOME_IMG = "https://images.unsplash.com/photo-1547205725-2a24bf99d0b8?w=800&h=500&fit=crop&auto=format";

const navy = "#061b3a";
const gold = "#eab53f";
const goldLight = "#f5c85b";
const border = "#d9dde5";

export default function Home() {
  return (
    <>
      {/* Hero */}
      <section style={{
        minHeight: 560,
        background: `linear-gradient(90deg, rgba(2,18,42,0.78) 0%, rgba(2,18,42,0.18) 100%), url(${HERO_IMG}) center 45%/cover no-repeat`,
        backgroundColor: navy,
        display: "flex",
        alignItems: "center",
      }}>
        <div style={{ width: "min(1400px,100%)", margin: "auto", padding: "80px 5%", color: "#fff" }}>
          <h1 style={{ maxWidth: 660, fontSize: "clamp(40px,5.5vw,70px)", lineHeight: 1.05, marginBottom: 20 }}>
            Welcome to<br />Moffat Bay Marina
          </h1>
          <div style={{ width: 85, height: 4, background: gold, marginBottom: 22 }} />
          <p style={{ fontSize: 20, fontWeight: 600, marginBottom: 6 }}>Premier Long-Term Marina Slip Reservations</p>
          <p style={{ fontSize: 17, marginBottom: 32, color: "#c8d4e8" }}>Safe Harbor. Beautiful Views. Exceptional Service.</p>
          <Link to="/reservations" style={{
            display: "inline-block",
            background: gold,
            color: navy,
            padding: "15px 30px",
            fontFamily: "Montserrat, Arial, sans-serif",
            fontWeight: 700,
            fontSize: 15,
            textDecoration: "none",
            letterSpacing: 0.5,
          }}>
            ⚓ &nbsp; RESERVE A SLIP
          </Link>
        </div>
      </section>

      {/* Feature strip */}
      <section style={{ background: "#fff", borderBottom: `1px solid ${border}`, display: "grid", gridTemplateColumns: "repeat(3,1fr)", maxWidth: 1400, margin: "0 auto" }}>
        {[
          { icon: "⛵", title: "Secure & Reliable", desc: "Well-maintained slips with 24/7 security and camera monitoring." },
          { icon: "⚡", title: "Full-Service Amenities", desc: "Electric, water, pump-out stations, and modern dock facilities." },
          { icon: "🤝", title: "Exceptional Service", desc: "Our experienced harbormaster team is here to make boating easy." },
        ].map(({ icon, title, desc }, i) => (
          <article key={title} style={{
            padding: "40px 32px",
            display: "flex",
            alignItems: "center",
            gap: 20,
            borderLeft: i > 0 ? `1px solid ${border}` : "none",
          }}>
            <div style={{
              width: 60, height: 60, minWidth: 60,
              borderRadius: "50%",
              background: navy,
              color: "#fff",
              display: "grid",
              placeItems: "center",
              fontSize: 26,
            }}>{icon}</div>
            <div>
              <h3 style={{ fontSize: 17, color: navy, marginBottom: 4 }}>{title}</h3>
              <p style={{ fontSize: 14, color: "#4a5568", margin: 0 }}>{desc}</p>
            </div>
          </article>
        ))}
      </section>

      {/* Welcome section */}
      <section style={{ maxWidth: 1250, margin: "0 auto", padding: "70px 5%", display: "grid", gridTemplateColumns: "1fr 1.15fr", gap: 70, alignItems: "center" }}>
        <div>
          <div style={{ color: "#c8921e", fontFamily: "Montserrat, Arial, sans-serif", fontWeight: 700, letterSpacing: 1, fontSize: 13, marginBottom: 8 }}>EXPLORE</div>
          <h2 style={{ color: navy, fontSize: "clamp(32px,4vw,48px)", lineHeight: 1.1, margin: "0 0 14px" }}>Moffat Bay Marina</h2>
          <div style={{ width: 50, height: 3, background: gold, marginBottom: 20 }} />
          <p style={{ maxWidth: 500, marginBottom: 26, color: "#4a5568" }}>
            Nestled on the beautiful shores of Moffat Bay, our marina offers long-term slips, outstanding amenities, and a welcoming community of boaters. We focus on dependable access, useful facilities, and customer service that helps make time on the water easier.
          </p>
          <Link to="/about" style={{
            display: "inline-block",
            background: "transparent",
            color: navy,
            border: `2px solid ${navy}`,
            padding: "14px 26px",
            fontFamily: "Montserrat, Arial, sans-serif",
            fontWeight: 700,
            fontSize: 14,
            textDecoration: "none",
            transition: "all 0.15s",
          }}>
            LEARN MORE ABOUT US &nbsp;→
          </Link>
        </div>
        <div style={{
          minHeight: 340,
          borderRadius: 10,
          background: `url(${WELCOME_IMG}) center 60%/cover no-repeat`,
          backgroundColor: "#8fa3c0",
          boxShadow: "0 10px 28px rgba(0,0,0,0.14)",
        }} role="img" aria-label="Moffat Bay Marina boats and docks" />
      </section>
    </>
  );
}
