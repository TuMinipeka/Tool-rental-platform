import { useEffect, useState } from 'react'
import { api } from '../api.js'

let featuresPromise
export function useFeatures() {
  const [features, setFeatures] = useState({ passwordRecovery: false, hostedCheckout: false })
  useEffect(() => {
    let active = true
    featuresPromise ??= api.features().catch(() => ({ passwordRecovery: false, hostedCheckout: false }))
    featuresPromise.then(value => { if (active) setFeatures({ passwordRecovery: value?.passwordRecovery === true, hostedCheckout: value?.hostedCheckout === true }) })
    return () => { active = false }
  }, [])
  return features
}
