import { useState, useEffect } from 'react'
import { supabase } from '../lib/supabase'
import { useAuth, useToast } from '../App'

export default function Notes() {
  const { user } = useAuth()
  const showToast = useToast()
  const [notes, setNotes] = useState([])
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(null)
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [search, setSearch] = useState('')

  useEffect(() => { loadNotes() }, [])

  async function loadNotes() {
    const { data } = await supabase
      .from('notes')
      .select('*')
      .eq('user_id', user.id)
      .order('is_pinned', { ascending: false })
      .order('updated_at', { ascending: false })
    setNotes(data || [])
    setLoading(false)
  }

  function startNew() {
    setEditing('new')
    setTitle('')
    setContent('')
  }

  function startEdit(note) {
    setEditing(note.id)
    setTitle(note.title)
    setContent(note.content)
  }

  async function save() {
    if (!title.trim() && !content.trim()) {
      setEditing(null)
      return
    }

    if (editing === 'new') {
      const { error } = await supabase.from('notes').insert({
        user_id: user.id,
        title: title.trim() || 'Untitled',
        content: content.trim(),
      })
      if (error) return showToast(error.message, 'error')
      showToast('Note created')
    } else {
      const { error } = await supabase.from('notes').update({
        title: title.trim() || 'Untitled',
        content: content.trim(),
        updated_at: new Date().toISOString(),
      }).eq('id', editing)
      if (error) return showToast(error.message, 'error')
      showToast('Note saved')
    }
    setEditing(null)
    loadNotes()
  }

  async function togglePin(note) {
    const { error } = await supabase.from('notes').update({
      is_pinned: !note.is_pinned,
      updated_at: new Date().toISOString(),
    }).eq('id', note.id)
    if (!error) loadNotes()
  }

  async function deleteNote(note) {
    if (!confirm(`Delete "${note.title || 'Untitled'}"?`)) return
    const { error } = await supabase.from('notes').delete().eq('id', note.id)
    if (error) return showToast(error.message, 'error')
    showToast('Note deleted')
    if (editing === note.id) setEditing(null)
    loadNotes()
  }

  const filtered = search
    ? notes.filter(n => n.title.toLowerCase().includes(search.toLowerCase()) || n.content.toLowerCase().includes(search.toLowerCase()))
    : notes

  if (loading) return <div className="spinner" />

  const isEditing = editing !== null

  return (
    <div>
      <div className="page-header">
        <h2>Notes</h2>
        {!isEditing && (
          <button className="btn btn-primary" onClick={startNew}>+ New Note</button>
        )}
      </div>

      {!isEditing && (
        <div className="search-bar">
          <input
            className="input"
            placeholder="Search notes…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
        </div>
      )}

      {isEditing ? (
        <div className="note-editor card">
          <div className="form-group">
            <label>Title</label>
            <input className="input" value={title} onChange={e => setTitle(e.target.value)} placeholder="Note title" />
          </div>
          <div className="form-group">
            <label>Content</label>
            <textarea className="input" value={content} onChange={e => setContent(e.target.value)} placeholder="Write something…" />
          </div>
          <div className="btn-group">
            <button className="btn btn-primary" onClick={save}>Save</button>
            <button className="btn btn-ghost" onClick={() => setEditing(null)}>Cancel</button>
            {editing !== 'new' && (
              <button className="btn btn-danger" onClick={() => deleteNote(notes.find(n => n.id === editing))} style={{ marginLeft: 'auto' }}>Delete</button>
            )}
          </div>
        </div>
      ) : (
        <>
          {filtered.length === 0 ? (
            <div className="empty-state">
              <h3>{search ? 'No matching notes' : 'No notes yet'}</h3>
              <p>{search ? 'Try a different search term.' : 'Create your first note to get started.'}</p>
            </div>
          ) : (
            <div className="notes-grid">
              {filtered.map(note => (
                <div key={note.id} className="note-card" onClick={() => startEdit(note)}>
                  <h3>
                    {note.title || 'Untitled'}
                    {note.is_pinned && <span className="pin-icon">📌</span>}
                  </h3>
                  <p>{note.content}</p>
                  <div className="date">
                    {new Date(note.updated_at).toLocaleDateString()}
                    <span style={{ float: 'right' }}>
                      <button className="btn btn-ghost btn-sm" onClick={e => { e.stopPropagation(); togglePin(note) }}>
                        {note.is_pinned ? 'Unpin' : 'Pin'}
                      </button>
                      <button className="btn btn-danger btn-sm" onClick={e => { e.stopPropagation(); deleteNote(note) }} style={{ marginLeft: '0.25rem' }}>
                        Delete
                      </button>
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  )
}
