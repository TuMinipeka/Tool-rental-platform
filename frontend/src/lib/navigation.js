

export function navigate(path){window.history.pushState({},'',path);window.dispatchEvent(new PopStateEvent('popstate'));const reducedMotion=window.matchMedia('(prefers-reduced-motion: reduce)').matches;window.scrollTo({top:0,behavior:reducedMotion?'auto':'smooth'})}
