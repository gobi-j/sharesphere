import { useEffect, useMemo, useState } from 'react'
import type { ChangeEvent, FormEvent } from 'react'
import { api, apiBaseUrl, getToken, saveToken } from './api'
import {
  ArrowDownUp, ArrowRight, Bell, Bookmark, Check, ChevronDown, CircleHelp, Clock3,
  Compass, Hammer, Heart, Home, Laptop, MapPin, Menu, MessageCircle, Plus,
  Search, ShieldCheck, Sparkles, Star, Users, X,
} from 'lucide-react'

type Listing = {
  id: string; title: string; category: string; price: number | null; unit: string
  distance: string; community: string; owner: string; initials: string; rating: number
  image: string; accent: string; availability: string; public: boolean; tag?: string; providerEmail?: string; exchangeType?: string; negotiable?: boolean
}
type BookingRecord = { id: string; listingId: string; requesterEmail: string; providerEmail: string; status: string; exchangeType: string; agreedPriceInr: number | null; startDate: string; endDate: string; reason: string; negotiation: Array<{ id: string; senderEmail: string; exchangeType: string; amountInr: number | null; reason: string; acceptedAt: string | null }> }
type DisputeRecord = { id: string; bookingId: string; openedBy: string; reason: string; status: string; resolvedBy: string | null; resolution: string | null }

const listings: Listing[] = [
  { id: 'demo-1', title: 'Cordless drill & bit set', category: 'Tools', price: 120, unit: 'day', distance: '0.4 km away', community: 'Indiranagar Neighbours', owner: 'Arjun Mehta', initials: 'AM', rating: 4.9, image: 'photo-1504148455328-c376907d081c', accent: '#e9e4d8', availability: 'Available today', public: true, tag: 'Popular' },
  { id: 'demo-2', title: 'Weekend camping tent', category: 'Outdoors', price: null, unit: 'free', distance: '0.7 km away', community: 'Indiranagar Neighbours', owner: 'Priya Shah', initials: 'PS', rating: 5.0, image: 'photo-1478131143081-80f7f84ca84d', accent: '#dbe5d5', availability: 'Available this weekend', public: true, tag: 'Free to borrow' },
  { id: 'demo-3', title: 'Canon EOS 200D camera', category: 'Electronics', price: 350, unit: 'day', distance: '1.2 km away', community: 'Domlur Community', owner: 'Rohan Iyer', initials: 'RI', rating: 4.8, image: 'photo-1516035069371-29a1b244cc32', accent: '#e2e2df', availability: 'Available tomorrow', public: true, tag: 'Verified' },
  { id: 'demo-4', title: 'Stand mixer, barely used', category: 'Home & kitchen', price: 80, unit: 'day', distance: '0.5 km away', community: 'Indiranagar Neighbours', owner: 'Nisha Rao', initials: 'NR', rating: 4.9, image: 'photo-1556911220-bff31c812dba', accent: '#efe2d8', availability: 'Available today', public: false },
  { id: 'demo-5', title: 'Ladder · 8 foot aluminium', category: 'Tools', price: null, unit: 'free', distance: '0.9 km away', community: 'Indiranagar Neighbours', owner: 'Dev Menon', initials: 'DM', rating: 4.7, image: 'photo-1504917595217-d4dc5ebe6122', accent: '#e6e5d7', availability: 'Available today', public: true },
  { id: 'demo-6', title: 'Portable projector', category: 'Electronics', price: 200, unit: 'day', distance: '2.1 km away', community: 'Domlur Community', owner: 'Meera Nair', initials: 'MN', rating: 5.0, image: 'photo-1478720568477-152d9b164e26', accent: '#d9e0df', availability: 'Available this week', public: true },
]

const categories = [
  { name: 'All items', icon: Compass }, { name: 'Tools', icon: Hammer },
  { name: 'Electronics', icon: Laptop }, { name: 'Home & kitchen', icon: Home },
  { name: 'Outdoors', icon: Sparkles },
]

const photo = (id: string) => `https://images.unsplash.com/${id}?auto=format&fit=crop&w=900&q=85`
const imageSrc = (image: string) => image.startsWith('photo-') ? photo(image) : /^https?:\/\//i.test(image) ? image : `${apiBaseUrl}${image.startsWith('/') ? '' : '/'}${image}`
const localToday = () => { const date = new Date(); date.setMinutes(date.getMinutes() - date.getTimezoneOffset()); return date.toISOString().slice(0, 10) }

function App() {
  const [category, setCategory] = useState('All items')
  const [query, setQuery] = useState('')
  const [nearby, setNearby] = useState(false)
  const [filter, setFilter] = useState<'Everything' | 'Free' | 'Paid'>('Everything')
  const [modal, setModal] = useState<Listing | 'create' | 'signin' | 'community' | null>(null)
  const [saved, setSaved] = useState<string[]>([])
  const [notice, setNotice] = useState('')
  const [mobileNav, setMobileNav] = useState(false)
  const [accessToken, setAccessToken] = useState(getToken())
  const [communityId, setCommunityId] = useState('')
  const [communityName, setCommunityName] = useState('Indiranagar Neighbours')
  const [userCommunities, setUserCommunities] = useState<Array<{ id: string; name: string; city: string; role: string }>>([])
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [upiVpa, setUpiVpa] = useState('')
  const [password, setPassword] = useState('')
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login')
  const [apiListings, setApiListings] = useState<Listing[] | null>(null)
  const [listingExchangeType, setListingExchangeType] = useState<'FREE' | 'PAID'>('PAID')
  const [listingPrice, setListingPrice] = useState('')
  const [listingNegotiable, setListingNegotiable] = useState(false)
  const [listingImageUrl, setListingImageUrl] = useState('')
  const [listingImagePreview, setListingImagePreview] = useState('')
  const [listingImageUploading, setListingImageUploading] = useState(false)
  const [listingImageError, setListingImageError] = useState(false)
  const [requestExchangeType, setRequestExchangeType] = useState<'FREE' | 'PAID'>('PAID')
  const [profileOpen, setProfileOpen] = useState(false)
  const [activeView, setActiveView] = useState<'discover' | 'exchanges' | 'messages' | 'notifications' | 'saved' | 'admin'>('discover')
  const [exchanges, setExchanges] = useState<BookingRecord[]>([])
  const [notifications, setNotifications] = useState<Array<{ id: string; message: string; type: string; read: boolean }>>([])
  const [chatBooking, setChatBooking] = useState('')
  const [chatMessages, setChatMessages] = useState<Array<{ id: string; senderEmail: string; content: string; sentAt: string }>>([])
  const [paymentLinks, setPaymentLinks] = useState<Record<string, string>>({})
  const [adminDisputes, setAdminDisputes] = useState<DisputeRecord[]>([])

  const sourceListings = apiListings ?? listings

  const visible = useMemo(() => sourceListings.filter(item => {
    const matchCategory = category === 'All items' || item.category === category
    const matchQuery = `${item.title} ${item.category} ${item.community} ${item.owner}`.toLowerCase().includes(query.toLowerCase())
    const matchScope = nearby ? item.community === communityName || item.public : item.community === communityName
    const matchPrice = filter === 'Everything' || (filter === 'Free' ? item.price === null : item.price !== null)
    return matchCategory && matchQuery && matchScope && matchPrice
  }), [category, communityName, filter, nearby, query, sourceListings])

  const toast = (message: string) => {
    setNotice(message)
    window.setTimeout(() => setNotice(''), 3000)
  }

  const toggleSaved = (id: string) => setSaved(current => current.includes(id) ? current.filter(item => item !== id) : [...current, id])

  const openCreateListing = () => {
    if (!accessToken || !communityId) { setModal(accessToken ? 'community' : 'signin'); return }
    if (listingImagePreview.startsWith('blob:')) URL.revokeObjectURL(listingImagePreview)
    setListingExchangeType('PAID'); setListingPrice(''); setListingNegotiable(false); setListingImageUrl(''); setListingImagePreview(''); setListingImageError(false); setListingImageUploading(false)
    setModal('create')
  }

  const openListingDetails = (item: Listing) => {
    setRequestExchangeType(item.price === null ? 'FREE' : 'PAID')
    setModal(item)
  }

  const handleListingImage = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.currentTarget.files?.[0]
    event.currentTarget.value = ''
    if (!file) return
    if (!['image/jpeg', 'image/png'].includes(file.type)) { if (listingImagePreview.startsWith('blob:')) URL.revokeObjectURL(listingImagePreview); setListingImagePreview(''); setListingImageUrl(''); setListingImageError(true); toast('Choose a JPEG or PNG image.'); return }
    if (file.size > 5 * 1024 * 1024) { if (listingImagePreview.startsWith('blob:')) URL.revokeObjectURL(listingImagePreview); setListingImagePreview(''); setListingImageUrl(''); setListingImageError(true); toast('Images must be 5 MB or smaller.'); return }
    if (listingImagePreview.startsWith('blob:')) URL.revokeObjectURL(listingImagePreview)
    const preview = URL.createObjectURL(file)
    setListingImagePreview(preview); setListingImageUrl(''); setListingImageError(false); setListingImageUploading(true)
    const body = new FormData(); body.set('file', file); body.set('communityId', communityId)
    try {
      const uploaded = await api<{ imageUrl: string }>('/api/borrowcircle/listings/images', { method: 'POST', body })
      setListingImageUrl(uploaded.imageUrl)
      toast('Image uploaded.')
    } catch (error) {
      URL.revokeObjectURL(preview); setListingImagePreview(''); setListingImageError(true)
      toast(error instanceof Error ? error.message : 'Could not upload image.')
    } finally { setListingImageUploading(false) }
  }

  const removeListingImage = () => {
    if (listingImagePreview.startsWith('blob:')) URL.revokeObjectURL(listingImagePreview)
    setListingImagePreview(''); setListingImageUrl(''); setListingImageError(false)
  }

  const closeModal = () => { if (modal === 'create') removeListingImage(); setModal(null) }

  const handleAuth = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    try {
      const auth = authMode === 'register'
        ? await api<{ accessToken: string; user: { fullName: string; email: string; role: string } }>('/api/auth/register', { method: 'POST', body: JSON.stringify({ fullName, email, password }) })
        : await api<{ accessToken: string; user: { fullName: string; email: string; role: string } }>('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
      saveToken(auth.accessToken)
      setAccessToken(auth.accessToken)
      setFullName(auth.user.fullName)
      setEmail(auth.user.email)
      setUserRole(auth.user.role)
      const inviteToken = window.location.pathname.startsWith('/invite/') ? window.location.pathname.split('/').pop() : null
      if (inviteToken) {
        const community = await api<{ id: string; name: string }>(`/api/communities/invitations/${inviteToken}/accept`, { method: 'POST' })
        setCommunityId(community.id)
        setCommunityName(community.name)
        setUserCommunities([{ id: community.id, name: community.name, city: '', role: 'MEMBER' }])
        window.history.replaceState({}, '', '/')
        setModal(null)
        toast(`You joined ${community.name}.`)
        return
      }
      const communities = await api<Array<{ id: string; name: string; city: string; role: string }>>('/api/communities')
      setUserCommunities(communities)
      if (communities.length) {
        setCommunityId(communities[0].id)
        setCommunityName(communities[0].name)
        setModal(null)
        toast(`Welcome back, ${auth.user.fullName.split(' ')[0]}.`)
      } else setModal('community')
    } catch (error) { toast(error instanceof Error ? error.message : 'Could not sign in.') }
  }

  const [userRole, setUserRole] = useState('MEMBER')
  useEffect(() => {
    if (!accessToken) return
    void Promise.all([
      api<{ fullName: string; email: string; role: string; upiVpa: string | null }>('/api/auth/profile'),
      api<Array<{ id: string; name: string; city: string; role: string }>>('/api/communities'),
    ]).then(([profile, communities]) => {
      setFullName(profile.fullName); setEmail(profile.email); setUserRole(profile.role); setUpiVpa(profile.upiVpa ?? '')
      setUserCommunities(communities)
      if (!communityId && communities.length) { setCommunityId(communities[0].id); setCommunityName(communities[0].name) }
      else if (!communities.length) setModal('community')
    }).catch(() => { localStorage.removeItem('sharesphere.accessToken'); setAccessToken('') })
  }, [accessToken])

  const handleProfileSave = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    try { await api('/api/auth/profile', { method: 'PUT', body: JSON.stringify({ fullName, upiVpa }) }); setModal(null); toast('Profile updated.') }
    catch (error) { toast(error instanceof Error ? error.message : 'Could not update your profile.') }
  }

  const handleCreateCommunity = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    try {
      const community = await api<{ id: string; name: string }>('/api/communities', { method: 'POST', body: JSON.stringify({ name: form.get('name'), city: form.get('city') }) })
      setCommunityId(community.id)
      setCommunityName(community.name)
      setUserCommunities(current => [...current, { id: community.id, name: community.name, city: String(form.get('city')), role: 'ADMIN' }])
      setModal(null)
      toast('Your community is ready. Invite neighbours to join.')
    } catch (error) { toast(error instanceof Error ? error.message : 'Could not create community.') }
  }

  const handleCreateListing = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!accessToken || !communityId) { setModal(accessToken ? 'community' : 'signin'); return }
    const form = new FormData(event.currentTarget)
    if (listingImageUploading) { toast('Wait for the image upload to finish.'); return }
    if (listingImageError) { toast('Remove the failed image or choose another image before publishing.'); return }
    const exchangeType = listingExchangeType
    const rawPrice = listingPrice.trim()
    const priceInr = exchangeType === 'FREE' ? null : Number(rawPrice)
    if (exchangeType === 'PAID' && (!Number.isFinite(priceInr) || priceInr! <= 0)) { toast('Enter a price greater than ₹0 for a paid listing.'); return }
    const availableFrom = String(form.get('availableFrom') ?? '') || null
    const availableTo = String(form.get('availableTo') ?? '') || null
    if (availableFrom && availableFrom < localToday()) { toast('Availability cannot start in the past.'); return }
    if (availableFrom && availableTo && availableTo < availableFrom) { toast('Available until must be on or after the start date.'); return }
    try {
      await api('/api/borrowcircle/listings', { method: 'POST', body: JSON.stringify({ title: form.get('title'), description: form.get('description') || `Shared by ${fullName || email}`, category: form.get('category'), communityId, communityName, visibility: form.get('visibility') === 'Public listing' ? 'PUBLIC' : 'COMMUNITY', exchangeType, negotiable: exchangeType === 'PAID' && listingNegotiable, priceInr, priceUnit: 'DAY', latitude: 12.9784, longitude: 77.6408, imageUrl: listingImageUrl || null, availableFrom, availableTo }) })
      closeModal()
      toast('Listing published to your community.')
      refreshListings()
    } catch (error) { toast(error instanceof Error ? error.message : 'Could not publish listing.') }
  }

  const handleRequest = async (event: FormEvent<HTMLFormElement>, listing: Listing) => {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    if (!accessToken || !communityId || !listing.providerEmail || !/^[0-9a-f-]{36}$/i.test(String(listing.id))) {
      setModal(null)
      toast('Sign in and choose a community to send a live request.')
      return
    }
    const exchangeType = requestExchangeType
    const submittedPrice = String(form.get('offerPrice') ?? '').trim()
    const proposedPriceInr = exchangeType === 'FREE' ? null : listing.negotiable ? Number(submittedPrice || listing.price || 0) || null : listing.price
    if (exchangeType === 'PAID' && !proposedPriceInr) { toast('Enter an INR amount for a paid request.'); return }
    const startDate = String(form.get('startDate') ?? '')
    const endDate = String(form.get('endDate') ?? '')
    if (startDate < localToday()) { toast('Borrow dates cannot start in the past.'); return }
    if (endDate < startDate) { toast('Return date must be on or after the borrow date.'); return }
    try {
      await api('/api/bookings', { method: 'POST', body: JSON.stringify({ listingId: listing.id, providerEmail: listing.providerEmail, exchangeType, proposedPriceInr, startDate, endDate, reason: form.get('message') }) })
      setModal(null)
      toast('Request sent — the provider will review it shortly.')
      void refreshExchanges()
    } catch (error) { toast(error instanceof Error ? error.message : 'Could not send request.') }
  }

  const refreshListings = async () => {
    if (!accessToken || !communityId) return
    try {
      const params = new URLSearchParams({ communityId, nearby: String(nearby) })
      if (query) params.set('q', query)
      if (category !== 'All items') params.set('category', category)
      params.set('latitude', '12.9784'); params.set('longitude', '77.6408')
      if (filter === 'Free') params.set('exchangeType', 'FREE')
      if (filter === 'Paid') params.set('exchangeType', 'PAID')
      const data = await api<Array<{ id: string; title: string; description: string; category: string; communityName: string; providerEmail: string; visibility: string; exchangeType: string; negotiable: boolean; price: number | null; imageUrl: string | null; distanceKm: number | null }>>(`/api/borrowcircle/listings?${params}`)
      setApiListings(data.map((item, index) => ({ id: item.id, title: item.title, category: item.category, price: item.price, unit: 'day', distance: item.distanceKm == null ? 'Nearby' : `${item.distanceKm} km away`, community: item.communityName, owner: item.providerEmail.split('@')[0], initials: item.providerEmail.slice(0, 2).toUpperCase(), rating: 5, image: item.imageUrl || listings[index % listings.length].image, accent: '#e9e4d8', availability: 'Ask provider', public: item.visibility === 'PUBLIC', providerEmail: item.providerEmail, exchangeType: item.exchangeType, negotiable: item.negotiable })))
    } catch { setApiListings(null) }
  }

  const refreshExchanges = async () => { if (!accessToken) return; try { setExchanges(await api<BookingRecord[]>('/api/bookings')) } catch { setExchanges([]) } }
  const refreshNotifications = async () => { if (!accessToken) return; try { setNotifications(await api<Array<{ id: string; message: string; type: string; read: boolean }>>('/api/notifications')) } catch { setNotifications([]) } }
  const bookingAction = async (id: string, action: string, body?: object) => { try { await api(`/api/bookings/${id}/${action}`, { method: 'POST', ...(body ? { body: JSON.stringify(body) } : {}) }); toast('Exchange updated.'); await refreshExchanges() } catch (error) { toast(error instanceof Error ? error.message : 'Could not update exchange.') } }
  const makeOffer = async (booking: BookingRecord) => { const price = window.prompt('Offer amount in INR (leave blank for free)'); if (price === null) return; const reason = window.prompt('Why are you suggesting these terms?'); if (!reason) return; const amount = price.trim() ? Number(price) : null; const exchangeType = amount === null ? 'FREE' : 'PAID'; try { await api(`/api/bookings/${booking.id}/offers`, { method: 'POST', body: JSON.stringify({ exchangeType, amountInr: amount, reason }) }); toast('Counteroffer sent.'); await refreshExchanges() } catch (error) { toast(error instanceof Error ? error.message : 'Could not send offer.') } }
  const openChat = async (bookingId: string) => { setChatBooking(bookingId); setActiveView('messages'); try { setChatMessages(await api(`/api/chat/${bookingId}/messages`)) } catch { setChatMessages([]) } }
  const sendChat = async (event: FormEvent<HTMLFormElement>) => { event.preventDefault(); const target = event.currentTarget; const form = new FormData(target); try { const message = await api<{ id: string; senderEmail: string; content: string; sentAt: string }>(`/api/chat/${chatBooking}/messages`, { method: 'POST', body: JSON.stringify({ content: form.get('content') }) }); setChatMessages(current => [...current, message]); target.reset() } catch (error) { toast(error instanceof Error ? error.message : 'Could not send message.') } }
  const startPayment = async (booking: BookingRecord) => { try { const intent = await api<{ upiUri: string }>(`/api/payments/upi/intents`, { method: 'POST', body: JSON.stringify({ bookingId: booking.id }) }); setPaymentLinks(current => ({ ...current, [booking.id]: intent.upiUri })); toast('UPI payment link is ready.'); } catch (error) { toast(error instanceof Error ? error.message : 'Could not start UPI payment.') } }
  const submitReview = async (booking: BookingRecord) => { const rating = Number(window.prompt('Rating from 1 to 5')); if (!rating || rating < 1 || rating > 5) return; const comment = window.prompt('Share a short review'); if (!comment) return; const subjectEmail = booking.requesterEmail.toLowerCase() === email.toLowerCase() ? booking.providerEmail : booking.requesterEmail; try { await api('/api/reviews', { method: 'POST', body: JSON.stringify({ bookingId: booking.id, subjectEmail, rating, comment }) }); toast('Thanks for sharing a review.') } catch (error) { toast(error instanceof Error ? error.message : 'Could not save review.') } }
  const refreshAdmin = async () => { if (userRole !== 'ADMIN') return; try { setAdminDisputes(await api<DisputeRecord[]>('/api/admin/disputes')) } catch (error) { toast(error instanceof Error ? error.message : 'Admin queue unavailable.') } }
  const resolveDispute = async (dispute: DisputeRecord) => { const resolution = window.prompt('Resolution summary'); if (!resolution) return; try { await api(`/api/admin/disputes/${dispute.id}/resolve`, { method: 'POST', body: JSON.stringify({ resolution }) }); toast('Dispute resolved.'); await refreshAdmin() } catch (error) { toast(error instanceof Error ? error.message : 'Could not resolve dispute.') } }

  useEffect(() => { void refreshListings() }, [accessToken, communityId, nearby, query, category, filter])
  useEffect(() => { void refreshExchanges(); void refreshNotifications() }, [accessToken])
  useEffect(() => { if (activeView === 'admin') void refreshAdmin() }, [activeView, userRole])
  useEffect(() => {
    if (activeView !== 'messages' || !chatBooking || !accessToken) return
    const controller = new AbortController()
    const connect = async () => {
      try {
        const response = await fetch(`${apiBaseUrl}/api/chat/${chatBooking}/stream`, { headers: { Authorization: `Bearer ${accessToken}`, Accept: 'text/event-stream' }, signal: controller.signal })
        if (!response.ok || !response.body) return
        const reader = response.body.getReader(), decoder = new TextDecoder()
        let buffer = ''
        while (!controller.signal.aborted) {
          const { value, done } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          let boundary = buffer.indexOf('\n\n')
          while (boundary >= 0) {
            const frame = buffer.slice(0, boundary); buffer = buffer.slice(boundary + 2)
            const data = frame.split('\n').find(line => line.startsWith('data:'))?.slice(5).trim()
            if (data) { try { const message = JSON.parse(data); if (message.id) setChatMessages(current => current.some(item => item.id === message.id) ? current : [...current, message]) } catch { /* ignore keepalive events */ } }
            boundary = buffer.indexOf('\n\n')
          }
        }
      } catch { /* stream closes when changing conversations or leaving the page */ }
    }
    void connect()
    return () => controller.abort()
  }, [activeView, chatBooking, accessToken])

  return <div className="app-shell">
    <aside className={`sidebar ${mobileNav ? 'sidebar-open' : ''}`}>
      <div className="brand"><span className="brand-mark"><ArrowDownUp size={20} strokeWidth={2.5} /></span><span>share<span className="brand-light">sphere</span></span></div>
      <button className="community-select" onClick={() => accessToken ? setModal('community') : setModal('signin')}><span className="community-avatar">{communityName.slice(0,1).toUpperCase()}</span><span className="community-copy"><b>{communityName}</b><small>Your community</small></span><ChevronDown size={15} /></button>
      <div className="side-label">YOUR SPACE</div>
      <nav className="side-nav">
        <button className={`side-link ${activeView === 'discover' ? 'active' : ''}`} onClick={() => setActiveView('discover')}><Compass size={18} /> Discover</button>
        <button className={`side-link ${activeView === 'exchanges' ? 'active' : ''}`} onClick={() => { setActiveView('exchanges'); void refreshExchanges() }}><ArrowDownUp size={18} /> My exchanges</button>
        <button className={`side-link ${activeView === 'messages' ? 'active' : ''}`} onClick={() => { if (exchanges[0]) void openChat(exchanges[0].id); else setActiveView('messages') }}><MessageCircle size={18} /> Messages</button>
        <button className={`side-link ${activeView === 'saved' ? 'active' : ''}`} onClick={() => setActiveView('saved')}><Bookmark size={18} /> Saved <span className="nav-badge">{saved.length}</span></button>
      </nav>
      <div className="side-label community-label">COMMUNITY</div>
      <nav className="side-nav">
        <button className="side-link" onClick={() => toast('Community members coming soon.')}><Users size={18} /> Members</button>
        <button className="side-link" onClick={async () => { if (!accessToken || !communityId) { setModal('signin'); return } const inviteEmail = window.prompt('Email address to invite'); if (!inviteEmail) return; try { const invite = await api<{ token: string }>(`/api/communities/${communityId}/invitations`, { method: 'POST', body: JSON.stringify({ email: inviteEmail }) }); await navigator.clipboard?.writeText(`${window.location.origin}/invite/${invite.token}`); toast(`Invitation link copied for ${inviteEmail}.`) } catch (error) { toast(error instanceof Error ? error.message : 'Could not send invitation.') } }}><Plus size={18} /> Invite neighbours</button>
      </nav>
      {userRole === 'ADMIN' && <><div className="side-label">ADMINISTRATION</div><nav className="side-nav"><button className={`side-link ${activeView === 'admin' ? 'active' : ''}`} onClick={() => setActiveView('admin')}><ShieldCheck size={18} /> Admin dashboard</button></nav></>}
      <div className="sidebar-bottom">
        <div className="impact-card"><div className="impact-icon"><Sparkles size={16} /></div><div><strong>Sharing adds up</strong><p>Your community has shared <b>248 items</b> this month.</p></div><div className="impact-line"><span /></div></div>
        <button className="side-link help-link" onClick={() => toast('Help centre coming soon.')}><CircleHelp size={18} /> Help & support</button>
        <button className="profile-mini" onClick={() => accessToken ? setProfileOpen(true) : setModal('signin')}><span className="user-avatar">{fullName ? fullName.split(' ').map(part => part[0]).slice(0,2).join('').toUpperCase() : 'GK'}</span><span><b>{fullName || 'Gobi Krishna'}</b><small>{accessToken ? 'Account settings' : 'Sign in'}</small></span><ChevronDown size={15} /></button>
      </div>
    </aside>

    {mobileNav && <button className="mobile-scrim" aria-label="Close navigation" onClick={() => setMobileNav(false)} />}

    <main className="main-content">
      <header className="topbar">
        <button className="mobile-menu icon-button" onClick={() => setMobileNav(!mobileNav)} aria-label="Menu"><Menu size={20} /></button>
        <div className="breadcrumbs"><span>BorrowCircle</span><span className="crumb-divider">/</span><b>{activeView === 'discover' ? 'Discover' : activeView === 'exchanges' ? 'My exchanges' : activeView === 'messages' ? 'Messages' : activeView === 'saved' ? 'Saved' : activeView === 'admin' ? 'Admin dashboard' : 'Notifications'}</b></div>
        <div className="top-actions"><button className="location-pill" onClick={() => setNearby(!nearby)}><MapPin size={15} /><span>{communityName}, Bengaluru</span><ChevronDown size={14} /></button><button className="icon-button notification-button" aria-label="Notifications" onClick={() => { setActiveView('notifications'); void refreshNotifications() }}><Bell size={19} /><i /></button><button className="top-avatar" onClick={() => accessToken ? setProfileOpen(true) : setModal('signin')}>{fullName ? fullName.split(' ').map(part => part[0]).slice(0,2).join('').toUpperCase() : 'GK'}</button></div>
      </header>

      <div className="content-wrap">
        <section className="hero">
          <div className="hero-copy"><div className="eyebrow"><span className="eyebrow-dot" /> YOUR NEIGHBOURHOOD, SHARED</div><h1>Good things are<br />closer than you think.</h1><p>Borrow what you need. Lend what you don't.<br className="desktop-break" /> A little sharing goes a long way.</p><div className="hero-actions"><button className="button-primary" onClick={openCreateListing}><Plus size={17} /> List something</button><span className="hero-note"><ShieldCheck size={16} /> Just for your community</span></div></div>
          <div className="hero-art"><div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" /><div className="art-sun" /><div className="art-shape shape-a"><Hammer size={40} /></div><div className="art-shape shape-b"><Laptop size={38} /></div><div className="art-shape shape-c"><Sparkles size={34} /></div><div className="art-leaf leaf-one" /><div className="art-leaf leaf-two" /><div className="art-stats"><span className="stats-icon"><Users size={15} /></span><span><b>1,240+</b><small>neighbours sharing</small></span><span className="stats-avatars"><i>AM</i><i>PS</i><i>RI</i><i>+8</i></span></div></div>
        </section>

        <section className="browse-section" style={{ display: activeView === 'discover' ? undefined : 'none' }}>
          <div className="section-heading"><div><div className="section-kicker">THE GOOD STUFF, NEAR YOU</div><h2>Find your next borrow</h2></div><button className={`nearby-toggle ${nearby ? 'toggle-on' : ''}`} onClick={() => setNearby(!nearby)}><span className="toggle-track"><i /></span><span>Include nearby communities</span><span className="toggle-info" title="Only public listings from other communities appear">i</span></button></div>
          <div className="search-row"><div className="search-box"><Search size={19} /><input value={query} onChange={event => setQuery(event.target.value)} placeholder="Try ‘drill’, ‘camera’, or ‘camping’" /><kbd>⌘ K</kbd></div><button className="filter-button" onClick={() => setFilter(filter === 'Paid' ? 'Everything' : 'Paid')}><ArrowDownUp size={17} /> Filters <span className="filter-count">{filter === 'Everything' ? '2' : '1'}</span></button></div>
          <div className="category-row">{categories.map(({ name, icon: Icon }) => <button key={name} className={`category-chip ${category === name ? 'selected' : ''}`} onClick={() => setCategory(name)}><Icon size={16} />{name}</button>)}</div>
          <div className="results-toolbar"><div className="result-count"><b>{visible.length} things</b><span> worth sharing nearby</span></div><div className="scope-tabs"><button className="scope-active" onClick={() => setFilter('Everything')}>All</button><button onClick={() => setFilter('Free')}>Free</button><button onClick={() => setFilter('Paid')}>Paid</button></div><button className="sort-button" onClick={() => toast('Sorted by closest to you.')}><span>Sort:</span> Nearest <ChevronDown size={14} /></button></div>

          {visible.length > 0 ? <div className="listing-grid">{visible.map(item => <article className="listing-card" key={item.id}>
            <button className="listing-image" onClick={() => openListingDetails(item)} style={{ backgroundColor: item.accent }} aria-label={`View ${item.title}`}><img src={imageSrc(item.image)} alt={item.title} loading="lazy" /><span className="availability"><i />{item.availability}</span>{item.tag && <span className={`listing-tag ${item.tag === 'Free to borrow' ? 'tag-free' : ''}`}>{item.tag === 'Popular' && <Sparkles size={12} />}{item.tag}</span>}{item.negotiable && <span className="listing-tag tag-negotiable">Negotiable</span>}</button>
            <div className="listing-info"><div className="listing-category">{item.category}<span>·</span>{item.distance}</div><div className="listing-title-row"><button className="listing-title" onClick={() => openListingDetails(item)}>{item.title}</button><button className={`save-button ${saved.includes(item.id) ? 'saved' : ''}`} aria-label="Save listing" onClick={() => toggleSaved(item.id)}><Heart size={17} fill={saved.includes(item.id) ? 'currentColor' : 'none'} /></button></div><div className="owner-row"><span className="owner-avatar">{item.initials}</span><span className="owner-name">{item.owner}</span><span className="rating"><Star size={13} fill="currentColor" />{item.rating.toFixed(1)}</span></div><div className="card-footer"><span className="community-name"><Users size={13} />{item.community}</span><span className="price">{item.price === null ? <><b>Free</b><small>to borrow</small></> : <><b>₹{item.price}</b><small>/{item.unit}</small></>}</span></div><button className="request-button" onClick={() => openListingDetails(item)}>Request to borrow <ArrowRight size={15} /></button>
          </div></article>)}</div> : <div className="empty-state"><div className="empty-icon"><Search size={22} /></div><h3>No matches just yet</h3><p>Try a different search or category. Your neighbours might have just the thing.</p><button onClick={() => { setCategory('All items'); setQuery(''); setFilter('Everything') }}>Clear filters</button></div>}
          <div className="more-row"><span>You're all caught up — check back soon for more.</span><span className="more-decoration"><i /><i /><i /></span></div>
        </section>

        {activeView === 'exchanges' && <section className="workspace-page"><div className="workspace-heading"><div><div className="section-kicker">BORROWCIRCLE TRANSACTIONS</div><h2>Your exchanges</h2></div><button className="filter-button" onClick={() => void refreshExchanges()}><ArrowDownUp size={16} /> Refresh</button></div>{!accessToken ? <div className="empty-state"><h3>Sign in to see your exchanges</h3><button onClick={() => setModal('signin')}>Sign in</button></div> : exchanges.length === 0 ? <div className="empty-state"><h3>No exchanges yet</h3><p>When you request an item or a neighbour requests yours, it will appear here.</p><button onClick={() => setActiveView('discover')}>Browse items</button></div> : <div className="exchange-list">{exchanges.map(booking => { const isProvider = booking.providerEmail.toLowerCase() === email.toLowerCase(); const latest = booking.negotiation.at(-1); const waitingOnRequester = booking.status === 'NEGOTIATING' && isProvider && latest?.senderEmail.toLowerCase() === booking.providerEmail.toLowerCase(); return <article className="exchange-card" key={booking.id}><div className="exchange-card-head"><div><div className="listing-category">{isProvider ? 'REQUEST RECEIVED' : 'REQUEST SENT'}<span>·</span>{booking.startDate}{booking.endDate !== booking.startDate && ` – ${booking.endDate}`}</div><h3>Borrow request · {booking.listingId.slice(0,8)}</h3></div><span className={`status-pill status-${booking.status.toLowerCase()}`}>{booking.status.replaceAll('_',' ')}</span></div><div className="exchange-participants"><span>{isProvider ? 'Requester' : 'Provider'}: <b>{isProvider ? booking.requesterEmail : booking.providerEmail}</b></span><span className="price">{booking.exchangeType === 'FREE' ? <b>Free exchange</b> : <>₹{booking.agreedPriceInr ?? 'Negotiable'} <small>INR</small></>}</span></div><p className="exchange-reason">{booking.reason}</p>{booking.negotiation.length > 1 && <div className="offer-history"><b>Negotiation</b>{booking.negotiation.map(offer => <div key={offer.id}><span>{offer.senderEmail === booking.requesterEmail ? 'Requester' : 'Provider'} offered {offer.exchangeType === 'FREE' ? 'free exchange' : `₹${offer.amountInr ?? '—'}`}</span><small>{offer.reason}</small></div>)}</div>}<div className="exchange-actions"><button className="outline-action" onClick={() => void openChat(booking.id)}><MessageCircle size={14} /> Message</button>{['REQUESTED','NEGOTIATING'].includes(booking.status) && <button className="outline-action" onClick={() => void makeOffer(booking)}><ArrowDownUp size={14} /> Counteroffer</button>}{waitingOnRequester && latest && <button className="button-primary" onClick={() => void bookingAction(booking.id,`offers/${latest.id}/accept`)}><Check size={14} /> Accept offer</button>}{isProvider && ['REQUESTED','NEGOTIATING'].includes(booking.status) && <><button className="button-primary" onClick={() => void bookingAction(booking.id,'approve')}><Check size={14} /> Approve</button><button className="outline-action" onClick={() => void bookingAction(booking.id,'decline')}>Decline</button></>}{!isProvider && ['REQUESTED','NEGOTIATING'].includes(booking.status) && <button className="outline-action" onClick={() => void bookingAction(booking.id,'cancel')}>Cancel request</button>}{booking.status === 'PENDING_PAYMENT' && !isProvider && <>{paymentLinks[booking.id] ? <a className="button-primary upi-link" href={paymentLinks[booking.id]}>Open UPI app</a> : <button className="button-primary" onClick={() => void startPayment(booking)}>Pay with UPI</button>}</>}{booking.status === 'READY_FOR_HANDOVER' && <button className="button-primary" onClick={() => void bookingAction(booking.id,'handover')}>Confirm handover</button>}{booking.status === 'ACTIVE' && <button className="outline-action" onClick={() => void bookingAction(booking.id,'return')}>Mark return requested</button>}{booking.status === 'RETURN_PENDING' && isProvider && <button className="button-primary" onClick={() => void bookingAction(booking.id,'confirm-return')}>Confirm return</button>}{['COMPLETED'].includes(booking.status) && <button className="outline-action" onClick={() => void submitReview(booking)}><Star size={14} /> Review</button>}{['READY_FOR_HANDOVER','ACTIVE','RETURN_PENDING'].includes(booking.status) && <button className="outline-action" onClick={() => { const reason = window.prompt('Describe the issue'); if (reason) void bookingAction(booking.id,'dispute',{ reason }) }}>Report issue</button>}</div></article> })}</div>}</section>}

        {activeView === 'messages' && <section className="workspace-page"><div className="workspace-heading"><div><div className="section-kicker">COORDINATE WITH YOUR NEIGHBOUR</div><h2>Messages</h2></div>{exchanges.length > 0 && <select className="chat-select" value={chatBooking} onChange={event => void openChat(event.target.value)}>{exchanges.map(booking => <option key={booking.id} value={booking.id}>Borrow request · {booking.listingId.slice(0,8)}</option>)}</select>}</div>{!accessToken ? <div className="empty-state"><h3>Sign in to message neighbours</h3><button onClick={() => setModal('signin')}>Sign in</button></div> : !chatBooking ? <div className="empty-state"><h3>Your conversations will appear here</h3><p>Start a conversation from one of your exchanges.</p></div> : <div className="chat-window"><div className="chat-messages">{chatMessages.map(message => <div className={`chat-message ${message.senderEmail.toLowerCase() === email.toLowerCase() ? 'chat-own' : ''}`} key={message.id}><small>{message.senderEmail}</small><p>{message.content}</p></div>)}</div><form className="chat-form" onSubmit={sendChat}><input name="content" required maxLength={2000} placeholder="Write a message..." /><button className="button-primary" type="submit">Send <ArrowRight size={14} /></button></form></div>}</section>}

        {activeView === 'notifications' && <section className="workspace-page"><div className="workspace-heading"><div><div className="section-kicker">KEEPING YOU IN THE LOOP</div><h2>Notifications</h2></div><button className="filter-button" onClick={() => void refreshNotifications()}><Bell size={16} /> Refresh</button></div>{notifications.length ? <div className="notification-list">{notifications.map(item => <article className={`notification-row ${item.read ? '' : 'unread'}`} key={item.id}><span className="notification-dot" /><div><b>{item.message}</b><small>{item.type.replaceAll('.',' ')}</small></div>{!item.read && <button className="outline-action" onClick={async () => { try { await api(`/api/notifications/${item.id}/read`,{method:'POST'}); await refreshNotifications() } catch (error) { toast(error instanceof Error ? error.message : 'Could not update notification.') } }}>Mark read</button>}</article>)}</div> : <div className="empty-state"><h3>You're all caught up</h3><p>Booking requests and updates will appear here.</p></div>}</section>}

        {activeView === 'saved' && <section className="workspace-page"><div className="workspace-heading"><div><div className="section-kicker">YOUR SHORTLIST</div><h2>Saved items</h2></div></div>{visible.filter(item => saved.includes(item.id)).length ? <div className="listing-grid">{visible.filter(item => saved.includes(item.id)).map(item => <article className="listing-card" key={item.id}><button className="listing-image" onClick={() => openListingDetails(item)} style={{backgroundColor:item.accent}}><img src={imageSrc(item.image)} alt={item.title} /></button><div className="listing-info"><div className="listing-title-row"><button className="listing-title" onClick={() => openListingDetails(item)}>{item.title}</button><button className="save-button saved" onClick={() => toggleSaved(item.id)}><Heart size={17} fill="currentColor" /></button></div><button className="request-button" onClick={() => openListingDetails(item)}>Request to borrow <ArrowRight size={15} /></button></div></article>)}</div> : <div className="empty-state"><h3>No saved items yet</h3><p>Tap the heart on a listing to save it for later.</p><button onClick={() => setActiveView('discover')}>Discover items</button></div>}</section>}

        {activeView === 'admin' && <section className="workspace-page"><div className="workspace-heading"><div><div className="section-kicker">COMMUNITY SAFETY</div><h2>Admin dashboard</h2></div><button className="filter-button" onClick={() => void refreshAdmin()}><ArrowDownUp size={15} /> Refresh</button></div><p className="modal-description">{adminDisputes.filter(d => d.status === 'OPEN').length} open disputes · {adminDisputes.length} total</p><div className="exchange-list">{adminDisputes.length ? adminDisputes.map(dispute => <article className="exchange-card" key={dispute.id}><div className="exchange-card-head"><div><div className="listing-category">BOOKING {dispute.bookingId.slice(0,8)}<span>·</span>OPENED BY {dispute.openedBy}</div><h3>Dispute case</h3></div><span className={`status-pill status-${dispute.status.toLowerCase()}`}>{dispute.status}</span></div><p className="exchange-reason">{dispute.reason}</p>{dispute.status === 'OPEN' && <button className="button-primary" onClick={() => void resolveDispute(dispute)}>Resolve dispute</button>}{dispute.resolution && <p className="modal-description">Resolution: {dispute.resolution}</p>}</article>) : <div className="empty-state"><h3>No disputes in the queue</h3></div>}</div></section>}

        <footer className="page-footer"><span>© {new Date().getFullYear()} ShareSphere</span><span>Made for neighbours, by neighbours <span className="footer-heart">♥</span></span><button onClick={() => toast('Community guidelines coming soon.')}>Community guidelines</button></footer>
      </div>
    </main>

    {notice && <div className="toast"><Check size={17} />{notice}</div>}
    {modal && <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) closeModal() }}><div className="modal-card">
      <button className="modal-close icon-button" onClick={closeModal} aria-label="Close"><X size={19} /></button>
      {modal === 'create' && <>
        <div className="modal-icon"><Plus size={20} /></div><div className="modal-eyebrow">GIVE SOMETHING A SECOND LIFE</div><h2>List an item</h2>
        <p className="modal-description">Your neighbours will thank you. Add a few details to get started.</p>
        <form onSubmit={handleCreateListing}>
          <label>What are you sharing?<input name="title" required placeholder="e.g. Cordless drill" /></label>
          <label>Category<select name="category"><option>Tools</option><option>Electronics</option><option>Home & kitchen</option><option>Outdoors</option><option>Books</option><option>Other</option></select></label>
          <label>Exchange type<select value={listingExchangeType} onChange={event => { const nextType = event.target.value as 'FREE' | 'PAID'; setListingExchangeType(nextType); if (nextType === 'FREE') { setListingPrice(''); setListingNegotiable(false) } }}><option value="PAID">Paid rental</option><option value="FREE">Free to borrow</option></select></label>
          {listingExchangeType === 'PAID' ? <><label>Price per day (INR)<input name="priceInr" type="number" min="0.01" step="0.01" required value={listingPrice} onChange={event => setListingPrice(event.target.value)} placeholder="e.g. 150" /></label><label className="checkbox-row"><input type="checkbox" checked={listingNegotiable} onChange={event => setListingNegotiable(event.target.checked)} /><span><b>Open to negotiation</b><small>Neighbours can suggest a different price or a free exchange with a reason.</small></span></label></> : <div className="price-disabled-note">No payment amount is needed for a free borrow.</div>}
          <label>Visibility<select name="visibility"><option>Community only</option><option>Public listing</option></select></label>
          <div className="form-row"><label>Available from<input name="availableFrom" type="date" min={localToday()} /></label><label>Available until<input name="availableTo" type="date" min={localToday()} /></label></div>
          <label>Short description<textarea name="description" maxLength={1600} rows={2} placeholder="Condition, what's included, and pickup notes" /></label>
          <div className="image-upload-box">{listingImagePreview ? <img className="image-upload-preview" src={listingImagePreview} alt="Listing preview" /> : <div className="image-upload-empty"><Plus size={19} /><span>No photo selected</span></div>}<div className="image-upload-copy"><b>Listing photo <small>Optional · JPEG or PNG · up to 5 MB</small></b><label className="image-upload-button">{listingImageUploading ? 'Uploading…' : listingImagePreview ? 'Replace image' : 'Choose image'}<input type="file" accept="image/jpeg,image/png" onChange={handleListingImage} disabled={listingImageUploading} /></label>{listingImagePreview && <button type="button" className="image-remove-button" onClick={removeListingImage} disabled={listingImageUploading}>Remove photo</button>}{listingImageError && <><small className="image-upload-error">This image could not be uploaded.</small><button type="button" className="image-remove-button" onClick={removeListingImage}>Clear and continue without a photo</button></>}</div></div>
          <button className="button-primary full-button" type="submit" disabled={listingImageUploading || listingImageError}>{listingImageUploading ? 'Uploading photo…' : 'Publish listing'} {!listingImageUploading && <ArrowRight size={16} />}</button>
        </form>
      </>}
      {modal === 'signin' && <>
        <div className="modal-icon"><Users size={20} /></div><div className="modal-eyebrow">WELCOME TO THE NEIGHBOURHOOD</div><h2>{authMode === 'login' ? 'Your community awaits' : 'Create your account'}</h2>
        <p className="modal-description">Sign in to request items, save favourites, and share with your neighbours.</p>
        <form onSubmit={handleAuth}>
          {authMode === 'register' && <label>Your name<input value={fullName} onChange={event => setFullName(event.target.value)} required minLength={2} placeholder="Full name" /></label>}
          <label>Email address<input type="email" value={email} onChange={event => setEmail(event.target.value)} required placeholder="you@example.com" /></label>
          <label>Password<input type="password" value={password} onChange={event => setPassword(event.target.value)} required minLength={8} placeholder="At least 8 characters" /></label>
          <button className="button-primary full-button" type="submit">{authMode === 'login' ? 'Continue with email' : 'Create account'} <ArrowRight size={16} /></button>
        </form>
        <button className="auth-switch" onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}>{authMode === 'login' ? 'New here? Create an account' : 'Already have an account? Sign in'}</button>
        <div className="modal-footnote"><ShieldCheck size={14} /> Invite-only communities keep sharing close to home.</div>
      </>}
      {modal === 'community' && <>
        <div className="modal-icon"><Users size={20} /></div><div className="modal-eyebrow">YOUR NEIGHBOURHOODS</div><h2>Choose a community</h2>
        <p className="modal-description">Browse stays focused on your selected community. You can create another one too.</p>
        {userCommunities.map(community => <button key={community.id} className="community-option" onClick={() => { setCommunityId(community.id); setCommunityName(community.name); setModal(null) }}><span className="community-avatar">{community.name.slice(0,1).toUpperCase()}</span><span><b>{community.name}</b><small>{community.city} · {community.role.toLowerCase()}</small></span>{community.id === communityId && <Check size={16} />}</button>)}
        <form onSubmit={handleCreateCommunity}><label>New community name<input name="name" required minLength={3} maxLength={100} placeholder="e.g. Indiranagar Neighbours" /></label><label>City<input name="city" required maxLength={80} placeholder="Bengaluru" /></label><button className="button-primary full-button" type="submit">Create community <ArrowRight size={16} /></button></form>
      </>}
      {typeof modal !== 'string' && <>
        <div className="detail-image" style={{ backgroundColor: modal.accent }}><img src={imageSrc(modal.image)} alt={modal.title} /></div>
        <div className="detail-topline"><span>{modal.category}</span><span><Star size={13} fill="currentColor" /> {modal.rating.toFixed(1)} · {modal.owner}</span></div>
        <h2>{modal.title}</h2><p className="modal-description"><MapPin size={14} /> {modal.distance} · {modal.community}</p>
        <div className="detail-price">{modal.price === null ? 'Free to borrow' : <>₹{modal.price}<small> / day</small></>}</div>
        {modal.negotiable && <div className="detail-negotiable">This provider is open to negotiation.</div>}
        <div className="detail-availability"><Clock3 size={15} /> {modal.availability}</div>
        <form onSubmit={event => handleRequest(event, modal)}>
          <label>Your offer or message<textarea name="message" required placeholder="Tell the provider what you need it for, and suggest any terms..." rows={3} /></label>
          <label>Offer type<select value={requestExchangeType} disabled={!modal.negotiable} onChange={event => setRequestExchangeType(event.target.value as 'FREE' | 'PAID')}><option value="FREE">Free exchange</option><option value="PAID">Paid · UPI after approval</option></select></label>
          {requestExchangeType === 'PAID' && <label>{modal.negotiable ? 'Your proposed amount (INR)' : 'Amount (INR)'}<input name="offerPrice" type="number" min="0.01" step="0.01" placeholder="Enter amount" defaultValue={modal.price ?? ''} disabled={!modal.negotiable} required={!modal.negotiable && modal.price == null} /></label>}
          {requestExchangeType === 'FREE' && <div className="price-disabled-note">No payment amount is needed for a free exchange.</div>}
          <div className="form-row"><label>Borrow from<input name="startDate" type="date" min={localToday()} defaultValue={localToday()} required /></label><label>Return by<input name="endDate" type="date" min={localToday()} defaultValue={localToday()} required /></label></div>
          <button className="button-primary full-button" type="submit">Send borrow request <ArrowRight size={16} /></button>
        </form>
        <div className="modal-footnote"><ShieldCheck size={14} /> The provider must approve before anything is confirmed.</div>
      </>}
    </div></div>}
    {profileOpen && <div className="modal-backdrop" onMouseDown={event => { if (event.target === event.currentTarget) setProfileOpen(false) }}><div className="modal-card"><button className="modal-close icon-button" onClick={() => setProfileOpen(false)} aria-label="Close"><X size={19} /></button><div className="modal-icon"><Users size={20} /></div><div className="modal-eyebrow">YOUR SHARESPHERE PROFILE</div><h2>Account settings</h2><p className="modal-description">Add a UPI ID to receive payments for paid borrows.</p><form onSubmit={handleProfileSave}><label>Display name<input value={fullName} onChange={event => setFullName(event.target.value)} required minLength={2} maxLength={100} /></label><label>Email address<input value={email} readOnly /></label><label>UPI ID<input value={upiVpa} onChange={event => setUpiVpa(event.target.value)} maxLength={100} placeholder="name@bank" /></label><button className="button-primary full-button" type="submit">Save profile <Check size={16} /></button></form><button className="auth-switch" onClick={() => { localStorage.removeItem('sharesphere.accessToken'); setAccessToken(''); setApiListings(null); setProfileOpen(false); toast('Signed out.') }}>Sign out</button></div></div>}
  </div>
}

export default App
