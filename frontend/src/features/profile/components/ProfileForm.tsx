import React, { useState, useEffect } from 'react';
import { useProfile, useUpdateProfile, UpdateProfilePayload } from '../hooks/useProfile';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Spinner } from '@/components/ui/Spinner';
import { User, Smartphone, Calendar, Save, Fingerprint } from 'lucide-react';
import toast from 'react-hot-toast';

export const ProfileForm: React.FC = () => {
  const { data: profile, isLoading } = useProfile();
  const { mutate: updateProfile, isPending } = useUpdateProfile();
  
  const [formData, setFormData] = useState<UpdateProfilePayload>({
    firstName: '',
    lastName: '',
    phoneNumber: '',
    birthDate: ''
  });

  useEffect(() => {
    if (profile) {
      setFormData({
        firstName: profile.firstName || '',
        lastName: profile.lastName || '',
        phoneNumber: profile.phoneNumber || '',
        birthDate: profile.birthDate || ''
      });
    }
  }, [profile]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfile(formData, {
      onSuccess: () => {
        toast.success('Identity profile updated.');
      },
      onError: (error: any) => {
        toast.error(error.message || 'Transmission failed.');
      }
    });
  };

  if (isLoading) return (
    <div className="flex justify-center p-20">
      <Spinner size="lg" />
    </div>
  );

  return (
    <form onSubmit={handleSubmit} className="space-y-12 max-w-3xl">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-10">
        <section className="space-y-6">
           <div className="flex items-center gap-3 mb-2">
              <User size={16} className="text-primary" />
              <h3 className="text-[10px] font-black uppercase tracking-[0.3em] text-muted-foreground">Core Identity</h3>
           </div>
           
           <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-[10px] font-black uppercase tracking-widest text-foreground/60 ml-1">Legal First Name</label>
                <Input
                  className="rounded-2xl h-14 bg-muted/30 border-border focus:bg-card transition-all font-bold px-6"
                  value={formData.firstName}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormData(prev => ({ ...prev, firstName: e.target.value }))}
                  placeholder="e.g. Alexander"
                />
              </div>
              <div className="space-y-2">
                <label className="text-[10px] font-black uppercase tracking-widest text-foreground/60 ml-1">Legal Last Name</label>
                <Input
                  className="rounded-2xl h-14 bg-muted/30 border-border focus:bg-card transition-all font-bold px-6"
                  value={formData.lastName}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormData(prev => ({ ...prev, lastName: e.target.value }))}
                  placeholder="e.g. Hamilton"
                />
              </div>
           </div>
        </section>

        <section className="space-y-6">
           <div className="flex items-center gap-3 mb-2">
              <Smartphone size={16} className="text-primary" />
              <h3 className="text-[10px] font-black uppercase tracking-[0.3em] text-muted-foreground">Contact & Verification</h3>
           </div>

           <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-[10px] font-black uppercase tracking-widest text-foreground/60 ml-1">Secure Mobile Number</label>
                <Input
                  className="rounded-2xl h-14 bg-muted/30 border-border focus:bg-card transition-all font-bold px-6"
                  value={formData.phoneNumber}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormData(prev => ({ ...prev, phoneNumber: e.target.value }))}
                  placeholder="+90 5XX XXX XX XX"
                />
              </div>
              <div className="space-y-2">
                <label className="text-[10px] font-black uppercase tracking-widest text-foreground/60 ml-1">Date of Birth</label>
                <div className="relative">
                   <Input
                    type="date"
                    className="rounded-2xl h-14 bg-muted/30 border-border focus:bg-card transition-all font-bold px-6 appearance-none"
                    value={formData.birthDate}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setFormData(prev => ({ ...prev, birthDate: e.target.value }))}
                   />
                   <Calendar className="absolute right-6 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" size={18} />
                </div>
              </div>
           </div>
        </section>
      </div>

      <div className="bg-primary/5 rounded-[2rem] p-8 flex items-start gap-6 border border-primary/10">
         <div className="w-12 h-12 bg-primary/20 rounded-2xl flex items-center justify-center shrink-0">
            <Fingerprint className="text-primary" size={24} />
         </div>
         <div className="space-y-1">
            <p className="text-sm font-black text-foreground uppercase tracking-tight">Identity Compliance</p>
            <p className="text-xs font-medium text-muted-foreground leading-relaxed">Updating your profile information may trigger a re-verification protocol. Ensure all details match your legal identification for uninterrupted service access.</p>
         </div>
      </div>

      <div className="pt-8 border-t border-border">
        <Button 
          type="submit" 
          disabled={isPending}
          className="h-16 px-12 rounded-2xl bg-primary hover:bg-primary/90 text-primary-foreground font-black text-xs uppercase tracking-[0.2em] shadow-xl shadow-primary/20 flex items-center gap-4 transition-all active:scale-95"
        >
          {isPending ? (
            <>
               <Spinner size="sm" className="text-white" />
               <span>Committing Changes...</span>
            </>
          ) : (
            <>
               <Save size={18} />
               <span>Synchronize Profile</span>
            </>
          )}
        </Button>
      </div>
    </form>
  );
};

export default ProfileForm;
