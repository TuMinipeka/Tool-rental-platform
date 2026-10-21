const paths = {
    eye:<><path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6S2 12 2 12Z"/><circle cx="12" cy="12" r="2.5"/></>,
    arrow:<><path d="M5 12h14"/><path d="m13 6 6 6-6 6"/></>, search:<><circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/></>,
    user:<><circle cx="12" cy="8" r="4"/><path d="M4 21c.6-4 3.3-6 8-6s7.4 2 8 6"/></>, calendar:<><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4M17 3v4M3 10h18"/></>,
    pin:<><path d="M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z"/><circle cx="12" cy="10" r="2"/></>, heart:<path d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.7-7.5a5.5 5.5 0 0 0 1.1-8.9Z"/>,
    check:<path d="m5 12 4 4L19 6"/>, shield:<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z"/>, menu:<><path d="M4 7h16M4 12h16M4 17h16"/></>, close:<><path d="m6 6 12 12M18 6 6 18"/></>,
    card:<><rect x="2" y="5" width="20" height="14" rx="2"/><path d="M2 10h20"/></>, paypal:<><path d="M9 19H5l3-14h6c4 0 6 2 5 5-.7 3.6-3 5-7 5H9.8L9 19Z"/><path d="m11 14-1 6h4l.6-3h2.2c2.8 0 4.6-1.2 5.2-4"/></>,
    bank:<><path d="m3 10 9-6 9 6"/><path d="M5 10v8M9.5 10v8M14.5 10v8M19 10v8M3 20h18"/></>, wallet:<><path d="M4 6h14a2 2 0 0 1 2 2v10H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h13"/><path d="M15 11h7v4h-7a2 2 0 0 1 0-4Z"/></>,
    grid:<><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></>, box:<><path d="m3 7 9 5 9-5M3 7l9-5 9 5v10l-9 5-9-5Z"/><path d="M12 12v10"/></>,
    hammer:<><path d="m14 6 4 4"/><path d="m16 3 5 5-4 4-5-5Z"/><path d="M14 10 5 21"/></>,
    mixer:<><path d="M5 5h8l5 5-5 6H7l-3-5Z"/><circle cx="9" cy="10" r="2"/><path d="m8 16-2 5M14 16l3 5M4 21h16"/></>,
    saw:<><circle cx="12" cy="12" r="7"/><circle cx="12" cy="12" r="2"/><path d="M12 5V2M17 7l2-2M19 12h3M17 17l2 2M12 19v3M7 17l-2 2M5 12H2M7 7 5 5"/></>,
    cleaning:<><path d="M12 3s-5 6-5 10a5 5 0 0 0 10 0c0-4-5-10-5-10Z"/><path d="M19 3v4M17 5h4M4 4v3M2.5 5.5h3"/></>,
    bolt:<path d="M13 2 4 14h7l-1 8 10-13h-7Z"/>,
    logout:<><path d="M10 17l5-5-5-5M15 12H3"/><path d="M14 3h7v18h-7"/></>, chevron:<path d="m9 18 6-6-6-6"/>,
  }


export function Icon({name,size=20}) {

  return <svg className="icon" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">{paths[name]}</svg>
}
