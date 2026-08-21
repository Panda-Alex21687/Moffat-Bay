import { createBrowserRouter } from "react-router";
import Layout from "./components/Layout";
import Home from "./pages/Home";
import About from "./pages/About";
import Contact from "./pages/Contact";
import Pricing from "./pages/Pricing";
import Availability from "./pages/Availability";
import Registration from "./pages/Registration";
import Reservation from "./pages/Reservation";
import ReservationSummary from "./pages/ReservationSummary";
import ReservationLookup from "./pages/ReservationLookup";

export const router = createBrowserRouter([
  {
    Component: Layout,
    children: [
      { index: true, Component: Home },
      { path: "about", Component: About },
      { path: "contact", Component: Contact },
      { path: "pricing", Component: Pricing },
      { path: "availability", Component: Availability },
      { path: "registration", Component: Registration },
      { path: "login", Component: Registration },
      { path: "reservations", Component: Reservation },
      { path: "reservation-summary", Component: ReservationSummary },
      { path: "reservation-lookup", Component: ReservationLookup },
    ],
  },
]);
