import { useState, useEffect, useRef } from 'react'
import { supabase } from '../lib/supabase'
import { useAuth, useToast } from '../App'

export default function Files() {
  const { user } = useAuth()
  const showToast = useToast()
  const [files, setFiles] = useState([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const fileInputRef = useRef()

  useEffect(() => {
    loadFiles()
  }, [])

  async function loadFiles() {
    const { data, error } = await supabase
      .from('files')
      .select('*')
      .eq('user_id', user.id)
      .order('created_at', { ascending: false })
    if (!error) setFiles(data || [])
    setLoading(false)
  }

  async function handleUpload(e) {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    showToast('Uploading…', 'success')

    const ext = file.name.split('.').pop() || ''
    const fileId = crypto.randomUUID()
    const remotePath = `${user.id}/files/${fileId}/${file.name}`

    const { error: uploadError } = await supabase.storage
      .from('vault-files')
      .upload(remotePath, file)

    if (uploadError) {
      showToast(`Upload failed: ${uploadError.message}`, 'error')
      setUploading(false)
      return
    }

    const { error: dbError } = await supabase.from('files').insert({
      user_id: user.id,
      name: file.name,
      extension: ext,
      mime_type: file.type || 'application/octet-stream',
      size: file.size,
      remote_path: remotePath,
    })

    if (dbError) {
      showToast(`Upload recorded but metadata failed: ${dbError.message}`, 'error')
    } else {
      showToast(`Uploaded ${file.name}`)
    }

    setUploading(false)
    fileInputRef.current.value = ''
    loadFiles()
  }

  async function handleDelete(file) {
    if (!confirm(`Delete ${file.name}?`)) return

    const { error: storageError } = await supabase.storage
      .from('vault-files')
      .remove([file.remote_path])

    if (storageError) {
      showToast(`Delete failed: ${storageError.message}`, 'error')
      return
    }

    await supabase.from('files').delete().eq('id', file.id)
    showToast(`Deleted ${file.name}`)
    loadFiles()
  }

  function getDownloadUrl(file) {
    const { data } = supabase.storage.from('vault-files').getPublicUrl(file.remote_path)
    return data.publicUrl
  }

  function formatSize(bytes) {
    if (!bytes) return '0 B'
    const k = 1024, sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
  }

  function getFileIcon(name) {
    const ext = name.split('.').pop()?.toLowerCase()
    if (['jpg','jpeg','png','gif','webp'].includes(ext)) return '🖼️'
    if (['mp4','mov','avi','mkv'].includes(ext)) return '🎬'
    if (['mp3','wav','flac','ogg'].includes(ext)) return '🎵'
    if (['pdf','doc','docx','txt'].includes(ext)) return '📄'
    if (['zip','rar','tar','gz','7z'].includes(ext)) return '📦'
    return '📁'
  }

  function getFileTag(name) {
    const ext = name.split('.').pop()?.toLowerCase()
    if (['jpg','jpeg','png','gif','webp'].includes(ext)) return <span className="tag tag-image">Image</span>
    if (['mp4','mov','avi','mkv'].includes(ext)) return <span className="tag tag-video">Video</span>
    if (['mp3','wav','flac','ogg'].includes(ext)) return <span className="tag tag-audio">Audio</span>
    if (['pdf','doc','docx','txt','rtf'].includes(ext)) return <span className="tag tag-document">Doc</span>
    if (['zip','rar','tar','gz','7z'].includes(ext)) return <span className="tag tag-archive">Archive</span>
    return null
  }

  if (loading) return <div className="spinner" />

  return (
    <div>
      <div className="page-header">
        <h2>Files</h2>
        <div>
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleUpload}
            style={{ display: 'none' }}
          />
          <button className="btn btn-primary" onClick={() => fileInputRef.current?.click()} disabled={uploading}>
            {uploading ? 'Uploading…' : '+ Upload File'}
          </button>
        </div>
      </div>

      {files.length === 0 ? (
        <div className="empty-state">
          <h3>No files yet</h3>
          <p>Upload your first file to get started.</p>
        </div>
      ) : (
        <div className="file-list">
          {files.map(file => (
            <div key={file.id} className="file-item">
              <div className="file-icon">{getFileIcon(file.name)}</div>
              <div className="file-info">
                <div className="name">
                  {file.name}
                  {getFileTag(file.name)}
                </div>
                <div className="meta">
                  {formatSize(file.size)} &middot; {new Date(file.created_at).toLocaleDateString()}
                </div>
              </div>
              <div className="file-actions">
                <a
                  className="btn btn-ghost btn-sm"
                  href={getDownloadUrl(file)}
                  target="_blank"
                  rel="noopener noreferrer"
                  download
                >
                  Download
                </a>
                <button className="btn btn-danger btn-sm" onClick={() => handleDelete(file)}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
