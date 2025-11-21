import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ContactService } from '../../../services/contact.service';
import { ContactMessage } from '../../../models/contact.model';

@Component({
  selector: 'app-contact-messages',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './contact-messages.component.html',
  styleUrl: './contact-messages.component.css'
})
export class ContactMessagesComponent implements OnInit {
  private readonly contactService = inject(ContactService);

  protected readonly messages = signal<ContactMessage[]>([]);
  protected readonly loading = signal(false);
  protected readonly selectedMessage = signal<ContactMessage | null>(null);
  protected readonly showUnreadOnly = signal(false);

  ngOnInit(): void {
    this.loadMessages();
  }

  loadMessages(): void {
    this.loading.set(true);
    this.contactService.getAllMessages().subscribe({
      next: (messages) => {
        this.messages.set(messages);
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.loading.set(false);
      }
    });
  }

  viewMessage(message: ContactMessage): void {
    this.selectedMessage.set(message);
  }

  closeModal(): void {
    this.selectedMessage.set(null);
  }

  deleteMessage(id: number): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce message?')) {
      this.contactService.deleteMessage(id).subscribe({
        next: () => {
          this.messages.set(this.messages().filter(m => m.id !== id));
          this.closeModal();
        },
        error: (error) => {
          console.error('Error deleting message:', error);
        }
      });
    }
  }

  get filteredMessages(): ContactMessage[] {
    return this.showUnreadOnly()
      ? this.messages().filter(m => !m.readStatus)
      : this.messages();
  }

  get unreadCount(): number {
    return this.messages().filter(m => !m.readStatus).length;
  }
}
