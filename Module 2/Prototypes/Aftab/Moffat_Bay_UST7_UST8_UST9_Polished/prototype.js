const defaultReservation={
 id:"MB-2026-00125",boatName:"Sea Explorer",boatLength:"34 ft",
 slipSize:"40 ft",slipNumber:"B12",checkIn:"09/15/2026",
 monthlyCost:350,status:"Confirmed"
};
function getReservation(){const x=localStorage.getItem("moffatBayReservation");return x?JSON.parse(x):defaultReservation}
function saveReservation(r){localStorage.setItem("moffatBayReservation",JSON.stringify(r))}
function statusHTML(s){return s==="Cancelled"?'<span class="status cancelled">Cancelled</span>':'<span class="status">Confirmed</span>'}
