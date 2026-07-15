import { useState, useEffect, createContext, useContext } from 'react'
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import { supabase } from './lib/supabase'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import Files from './pages/Files'
import Notes from './pages/Notes'

export const AuthContext = createContext(null)
export const ToastContext = createContext(null)

export function useAuth() { return useContext(AuthContext) }
export function useToast() { return useContext(ToastContext) }

export default function App() {
  const [session, setSession] = useState(null)
  const [loading, setLoading] = useState(true)
  const [toast, setToast] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session)
      setLoading(false)
    })
    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session)
    })
    return () => subscription.unsubscribe()
  }, [])

  const showToast = (message, type = 'success') => {
    setToast({ message, type })
    setTimeout(() => setToast(null), 4000)
  }

  if (loading) return <div style={{ display:'flex', alignItems:'center', justifyContent:'center', minHeight:'100vh' }}><div className="spinner" /></div>

  return (
    <AuthContext.Provider value={{ session, user: session?.user }}>
      <ToastContext.Provider value={showToast}>
        {session && <NavBar />}
        <div className="container">
          <Routes>
            <Route path="/login" element={session ? <Navigate to="/" /> : <Login />} />
            <Route path="/register" element={session ? <Navigate to="/" /> : <Register />} />
            <Route path="/" element={session ? <Dashboard /> : <Navigate to="/login" />} />
            <Route path="/files" element={session ? <Files /> : <Navigate to="/login" />} />
            <Route path="/files/*" element={session ? <Files /> : <Navigate to="/login" />} />
            <Route path="/notes" element={session ? <Notes /> : <Navigate to="/login" />} />
            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </div>
        {toast && <div className={`toast toast-${toast.type}`}>{toast.message}</div>}
      </ToastContext.Provider>
    </AuthContext.Provider>
  )
}

function NavBar() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const { pathname } = window.location

  const handleLogout = async () => {
    await supabase.auth.signOut()
    navigate('/login')
  }

  return (
    <nav className="nav">
      <div className="nav-inner">
        <a href="/" className="nav-brand">ShantyVault</a>
        <div className="nav-links">
          <a href="/" className={pathname === '/' ? 'active' : ''}>Dashboard</a>
          <a href="/files" className={pathname.startsWith('/files') ? 'active' : ''}>Files</a>
          <a href="/notes" className={pathname === '/notes' ? 'active' : ''}>Notes</a>
        </div>
        <div className="nav-user">
          <span>{user?.email}</span>
          <button className="btn btn-ghost btn-sm" onClick={handleLogout}>Logout</button>
        </div>
      </div>
    </nav>
  )
}
