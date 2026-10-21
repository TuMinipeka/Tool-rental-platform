import { useEffect, useState } from 'react'
import { api } from '../api.js'

let featuresPromise
export function useFeatures() {
  const [features, setFeatures] = useState({ hostedCheckout: false, paymentProvider: null })
  useEffect(() => {
    let active = true
    featuresPromise ??= api.features().catch(() => ({ hostedCheckout: false, paymentProvider: null }))
    featuresPromise.then(value => { if (active) setFeatures({ hostedCheckout: value?.hostedCheckout === true, paymentProvider: value?.paymentProvider || null }) })
    return () => { active = false }
  }, [])
  return features
}
