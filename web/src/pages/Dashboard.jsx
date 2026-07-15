import { useState, useEffect } from 'react'
import { supabase } from '../lib/supabase'
import { useAuth } from '../App'

export default function Dashboard() {
  const { user } = useAuth()
  const [files, setFiles] = useState([])
  const [notes, setNotes] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  async function loadData() {
    const { data: { user: u } } = await supabase.auth.getUser()
    const userId = u?.id
    if (!userId) return

    const { data: filesData } = await supabase.storage.from('vault-files').list(`${userId}/files`, { limit: 100 })
    const fileList = filesData || []

    const { data: notesData } = await supabase.from('notes').select('*').eq('user_id', userId).order('updated_at', { ascending: false })
    setFiles(fileList)
    setNotes(notesData || [])
    setLoading(false)
  }

  const totalSize = files.reduce((sum, f) => sum + (f.metadata?.size || 0), 0)
  const storageLimit = 5 * 1024 * 1024 * 1024
  const percentUsed = Math.min(100, (totalSize / storageLimit) * 100)

  function formatSize(bytes) {
    if (bytes === 0) return '0 B'
    const k = 1024, sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
  }

  if (loading) return <div className="spinner" />

  return (
    <div>
      <div className="page-header">
        <h2>Dashboard</h2>
        <span style={{ color: '#94a3b8' }}>Welcome, {user?.user_metadata?.name || user?.email}</span>
      </div>

      <div className="grid">
        <div className="stat-card">
          <h3>Storage Used</h3>
          <div className="value">{formatSize(totalSize)}</div>
          <div className="bar-bg"><div className="bar-fill" style={{ width: `${percentUsed}%` }} /></div>
          <div style={{ marginTop: '0.5rem', fontSize: '0.85rem', color: '#94a3b8' }}>
            {percentUsed.toFixed(1)}% of {formatSize(storageLimit)}
          </div>
        </div>
        <div className="stat-card">
          <h3>Files</h3>
          <div className="value">{files.length}</div>
        </div>
        <div className="stat-card">
          <h3>Notes</h3>
          <div className="value">{notes.length}</div>
        </div>
      </div>

      <div style={{ marginTop: '2rem' }}>
        <h3 style={{ marginBottom: '1rem' }}>Recent Notes</h3>
        {notes.length === 0 ? (
          <div className="empty-state"><h3>No notes yet</h3><p>Create your first note to get started.</p></div>
        ) : (
          <div className="notes-grid">
            {notes.slice(0, 6).map(note => (
              <div key={note.id} className="note-card" onClick={() => window.location = '/notes'}>
                <h3>{note.title || 'Untitled'}</h3>
                <p>{note.content}</p>
                <div className="date">{new Date(note.updated_at).toLocaleDateString()}</div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
