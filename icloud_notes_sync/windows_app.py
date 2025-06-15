import tkinter as tk
from tkinter import ttk, messagebox
import os
import json
from sync_core import iCloudNotesSync

class NotesApp:
    def __init__(self, root):
        self.root = root
        self.root.title("iCloud Notes Sync")
        self.root.geometry("800x600")
        
        self.sync_core = iCloudNotesSync()
        self.notes_data = []
        self.local_storage = os.path.join(os.path.expanduser("~"), "iCloudNotesSync")
        
        self._create_ui()
        
    def _create_ui(self):
        # Login frame
        login_frame = ttk.LabelFrame(self.root, text="iCloud Login")
        login_frame.pack(fill="x", padx=10, pady=10)
        
        ttk.Label(login_frame, text="Apple ID:").grid(row=0, column=0, padx=5, pady=5)
        self.apple_id_var = tk.StringVar()
        ttk.Entry(login_frame, textvariable=self.apple_id_var).grid(row=0, column=1, padx=5, pady=5)
        
        ttk.Label(login_frame, text="Password:").grid(row=1, column=0, padx=5, pady=5)
        self.password_var = tk.StringVar()
        ttk.Entry(login_frame, textvariable=self.password_var, show="*").grid(row=1, column=1, padx=5, pady=5)
        
        ttk.Button(login_frame, text="Login", command=self.login).grid(row=2, column=0, columnspan=2, pady=10)
        
        # Notes list
        notes_frame = ttk.Frame(self.root)
        notes_frame.pack(fill="both", expand=True, padx=10, pady=10)
        
        self.notes_list = ttk.Treeview(notes_frame, columns=("title", "updated"), show="headings")
        self.notes_list.heading("title", text="Title")
        self.notes_list.heading("updated", text="Last Updated")
        self.notes_list.pack(side="left", fill="both", expand=True)
        
        scrollbar = ttk.Scrollbar(notes_frame, orient="vertical", command=self.notes_list.yview)
        scrollbar.pack(side="right", fill="y")
        self.notes_list.configure(yscrollcommand=scrollbar.set)
        
        # Sync button
        ttk.Button(self.root, text="Sync Notes", command=self.sync_notes).pack(pady=10)
        
    def login(self):
        apple_id = self.apple_id_var.get()
        password = self.password_var.get()
        
        if not apple_id or not password:
            messagebox.showerror("Error", "Please enter both Apple ID and password")
            return
            
        if self.sync_core.authenticate(apple_id, password):
            messagebox.showinfo("Success", "Login successful")
            self.sync_notes()
        else:
            messagebox.showerror("Error", "Login failed")
    
    def sync_notes(self):
        try:
            self.notes_data = self.sync_core.sync_to_local(self.local_storage)
            self.update_notes_list()
            messagebox.showinfo("Success", f"Synced {len(self.notes_data)} notes")
        except Exception as e:
            messagebox.showerror("Error", f"Sync failed: {str(e)}")
    
    def update_notes_list(self):
        self.notes_list.delete(*self.notes_list.get_children())
        for note in self.notes_data:
            self.notes_list.insert("", "end", values=(note["title"], note["updated"]))

if __name__ == "__main__":
    root = tk.Tk()
    app = NotesApp(root)
    root.mainloop()