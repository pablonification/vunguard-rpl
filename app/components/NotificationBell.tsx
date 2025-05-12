import { useState, useEffect } from 'react';
import { Bell } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { getUnreadNotificationCount } from '@/lib/db/models/notification';
import NotificationPanel from './NotificationPanel';

interface NotificationBellProps {
  userId: number;
}

export default function NotificationBell({ userId }: NotificationBellProps) {
  const [unreadCount, setUnreadCount] = useState(0);
  const [isOpen, setIsOpen] = useState(false);

  // Fetch unread count on mount and when notifications are marked as read
  useEffect(() => {
    const fetchUnreadCount = async () => {
      try {
        const count = await getUnreadNotificationCount(userId);
        setUnreadCount(count);
      } catch (error) {
        console.error('Failed to fetch unread count:', error);
      }
    };

    fetchUnreadCount();
    // Set up polling every minute
    const interval = setInterval(fetchUnreadCount, 60000);
    return () => clearInterval(interval);
  }, [userId]);

  return (
    <div className="relative">
      <Button
        variant="ghost"
        size="icon"
        className="relative h-9 w-9 rounded-full"
        onClick={() => setIsOpen(!isOpen)}
      >
        <Bell className="h-[1.2rem] w-[1.2rem]" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 h-4 w-4 rounded-full bg-red-500 text-[10px] font-medium text-white flex items-center justify-center">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </Button>

      {isOpen && (
        <>
          {/* Backdrop for closing notifications */}
          <div 
            className="fixed inset-0 z-40"
            onClick={() => setIsOpen(false)}
          />
          {/* Notification Panel Container with white background */}
          <div className="absolute right-0 mt-2 z-50 bg-white dark:bg-card rounded-lg shadow-lg border border-border">
            <NotificationPanel 
              userId={userId} 
              onClose={() => setIsOpen(false)}
              onNotificationRead={() => {
                // Refresh unread count when a notification is marked as read
                getUnreadNotificationCount(userId).then(setUnreadCount);
              }}
            />
          </div>
        </>
      )}
    </div>
  );
} 